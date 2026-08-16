package xyz.crt572.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.crt572.quotes.data.Playlist
import xyz.crt572.quotes.data.Quote
import xyz.crt572.quotes.data.QuoteDatabase
import xyz.crt572.quotes.data.QuotePlaylistCrossRef

enum class Tab { Quotes, Playlists }

sealed interface SheetMode {
    data object Hidden : SheetMode
    data class Adding(val playlistIds: Set<Int> = emptySet()) : SheetMode
    data class Editing(val quote: Quote, val playlistIds: Set<Int>) : SheetMode
}

data class QuotesUiState(
    val quotes: List<Quote> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val tab: Tab = Tab.Quotes,
    val openPlaylist: Playlist? = null,
    val playlistQuotes: List<Quote> = emptyList(),
    val sheet: SheetMode = SheetMode.Hidden,
    val pendingUndo: Quote? = null,
)

class QuotesViewModel(db: QuoteDatabase) : ViewModel() {

    private val quoteDao = db.quoteDao()
    private val playlistDao = db.playlistDao()

    private data class Local(
        val tab: Tab = Tab.Quotes,
        val openPlaylistId: Int? = null,
        val sheet: SheetMode = SheetMode.Hidden,
        // quote + the playlist ids it was in, so undo restores memberships too
        val pendingUndo: Pair<Quote, Set<Int>>? = null,
    )

    private val local = MutableStateFlow(Local())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val playlistQuotes = local
        .map { it.openPlaylistId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else quoteDao.getInPlaylist(id)
        }

    val state: StateFlow<QuotesUiState> = combine(
        quoteDao.getAll(), playlistDao.getAll(), playlistQuotes, local,
    ) { quotes, playlists, inPlaylist, l ->
        QuotesUiState(
            quotes = quotes,
            playlists = playlists,
            tab = l.tab,
            openPlaylist = playlists.find { it.id == l.openPlaylistId },
            playlistQuotes = inPlaylist,
            sheet = l.sheet,
            pendingUndo = l.pendingUndo?.first,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuotesUiState())

    fun selectTab(tab: Tab) = local.update { it.copy(tab = tab) }

    fun openPlaylist(id: Int) = local.update { it.copy(openPlaylistId = id) }

    fun closePlaylist() = local.update { it.copy(openPlaylistId = null) }

    fun startAdd() = local.update { l ->
        val preselect = if (l.tab == Tab.Playlists) setOfNotNull(l.openPlaylistId) else emptySet()
        l.copy(sheet = SheetMode.Adding(preselect))
    }

    fun startEdit(quote: Quote) {
        viewModelScope.launch {
            val ids = playlistDao.playlistIdsForQuote(quote.id).first().toSet()
            local.update { it.copy(sheet = SheetMode.Editing(quote, ids)) }
        }
    }

    fun dismissSheet() = local.update { it.copy(sheet = SheetMode.Hidden) }

    fun save(quote: Quote, playlistIds: Set<Int>) {
        val before = (local.value.sheet as? SheetMode.Editing)?.playlistIds ?: emptySet()
        dismissSheet()
        viewModelScope.launch {
            val id = if (quote.id == 0) {
                quoteDao.insert(quote).toInt()
            } else {
                quoteDao.update(quote)
                quote.id
            }
            val added = (playlistIds - before).map { QuotePlaylistCrossRef(id, it) }
            if (added.isNotEmpty()) playlistDao.addRefs(added)
            (before - playlistIds).forEach { playlistDao.removeRefs(listOf(id), it) }
        }
    }

    fun delete(quote: Quote) {
        viewModelScope.launch {
            val refs = playlistDao.playlistIdsForQuote(quote.id).first().toSet()
            quoteDao.delete(quote)
            local.update { it.copy(pendingUndo = quote to refs) }
        }
    }

    fun undoDelete() {
        val (quote, refs) = local.value.pendingUndo ?: return
        local.update { it.copy(pendingUndo = null) }
        viewModelScope.launch {
            // insert with the original non-zero id bypasses autogenerate
            quoteDao.insert(quote)
            if (refs.isNotEmpty()) playlistDao.addRefs(refs.map { QuotePlaylistCrossRef(quote.id, it) })
        }
    }

    fun clearUndo() = local.update { it.copy(pendingUndo = null) }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.insert(Playlist(name = name.trim(), sortOrder = playlistDao.maxSortOrder() + 1))
        }
    }

    fun renamePlaylist(playlist: Playlist, name: String) {
        viewModelScope.launch { playlistDao.update(playlist.copy(name = name.trim())) }
    }

    fun deletePlaylist(playlist: Playlist) {
        local.update { if (it.openPlaylistId == playlist.id) it.copy(openPlaylistId = null) else it }
        viewModelScope.launch { playlistDao.delete(playlist) }
    }
}
