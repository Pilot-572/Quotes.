package xyz.crt572.quotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Int): Playlist?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM playlists")
    suspend fun maxSortOrder(): Int

    @Insert
    suspend fun insert(playlist: Playlist): Long

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRefs(refs: List<QuotePlaylistCrossRef>)

    @Query("DELETE FROM quote_playlist_cross_ref WHERE quoteId IN (:quoteIds) AND playlistId = :playlistId")
    suspend fun removeRefs(quoteIds: List<Int>, playlistId: Int)

    @Query("SELECT playlistId FROM quote_playlist_cross_ref WHERE quoteId = :quoteId")
    fun playlistIdsForQuote(quoteId: Int): Flow<List<Int>>
}
