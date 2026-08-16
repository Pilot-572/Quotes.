package xyz.crt572.quotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.crt572.quotes.QuotesViewModel
import xyz.crt572.quotes.SheetMode
import xyz.crt572.quotes.Tab
import xyz.crt572.quotes.data.Quote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesApp(vm: QuotesViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showCreatePlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(state.pendingUndo) {
        state.pendingUndo ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = "Quote deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) vm.undoDelete() else vm.clearUndo()
    }

    val inPlaylistDetail = state.tab == Tab.Playlists && state.openPlaylist != null
    BackHandler(enabled = inPlaylistDetail) { vm.closePlaylist() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inPlaylistDetail) state.openPlaylist!!.name else "Quotes.") },
                navigationIcon = {
                    if (inPlaylistDetail) {
                        IconButton(onClick = vm::closePlaylist) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.tab == Tab.Quotes,
                    onClick = { vm.selectTab(Tab.Quotes) },
                    icon = { Icon(Icons.Filled.FormatQuote, contentDescription = null) },
                    label = { Text("Quotes") },
                )
                NavigationBarItem(
                    selected = state.tab == Tab.Playlists,
                    onClick = { vm.selectTab(Tab.Playlists) },
                    icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                    label = { Text("Playlists") },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (state.tab == Tab.Quotes || inPlaylistDetail) vm.startAdd()
                else showCreatePlaylist = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.tab == Tab.Quotes -> QuoteList(
                quotes = state.quotes,
                onEdit = vm::startEdit,
                onDelete = vm::delete,
                emptyTitle = "No quotes yet",
                emptyHint = "Tap + to add your first one",
                modifier = Modifier.padding(padding),
            )

            inPlaylistDetail -> QuoteList(
                quotes = state.playlistQuotes,
                onEdit = vm::startEdit,
                onDelete = vm::delete,
                emptyTitle = "Nothing here yet",
                emptyHint = "Tap + to add a quote to this playlist",
                modifier = Modifier.padding(padding),
            )

            else -> PlaylistsScreen(
                playlists = state.playlists,
                onOpen = vm::openPlaylist,
                onCreate = vm::createPlaylist,
                onRename = vm::renamePlaylist,
                onDelete = vm::deletePlaylist,
                showCreateDialog = showCreatePlaylist,
                onDismissCreateDialog = { showCreatePlaylist = false },
                modifier = Modifier.padding(padding),
            )
        }
    }

    when (val sheet = state.sheet) {
        SheetMode.Hidden -> {}
        is SheetMode.Adding -> QuoteSheet(
            initial = null,
            initialPlaylistIds = sheet.playlistIds,
            playlists = state.playlists,
            onSave = vm::save,
            onDismiss = vm::dismissSheet,
        )

        is SheetMode.Editing -> QuoteSheet(
            initial = sheet.quote,
            initialPlaylistIds = sheet.playlistIds,
            playlists = state.playlists,
            onSave = vm::save,
            onDismiss = vm::dismissSheet,
        )
    }
}

@Composable
private fun QuoteList(
    quotes: List<Quote>,
    onEdit: (Quote) -> Unit,
    onDelete: (Quote) -> Unit,
    emptyTitle: String,
    emptyHint: String,
    modifier: Modifier = Modifier,
) {
    if (quotes.isEmpty()) {
        EmptyState(emptyTitle, emptyHint, modifier)
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(quotes, key = { it.id }) { quote ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) onDelete(quote)
                    // ponytail: always veto so the box never sits in "dismissed" —
                    // Room's flow removes the row, and undo re-adds it without a stale state.
                    false
                },
            )
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 24.dp),
                        )
                    }
                },
            ) {
                QuoteListItem(quote = quote, onClick = { onEdit(quote) })
            }
        }
    }
}

@Composable
private fun QuoteListItem(quote: Quote, onClick: () -> Unit) {
    // opaque surface so the swipe background stays hidden until the row actually moves
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                ),
            )
            val attribution = listOfNotNull(
                quote.author?.takeIf { it.isNotBlank() },
                quote.source?.takeIf { it.isNotBlank() },
            ).joinToString(", ")
            if (attribution.isNotEmpty()) {
                Text(
                    text = "— $attribution",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
internal fun EmptyState(title: String, hint: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
