package xyz.crt572.quotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY id DESC")
    fun getAll(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Quote?

    // playlistId null = rotate over all quotes; excludeId avoids repeating the current one.
    @Query(
        """
        SELECT q.id FROM quotes q
        WHERE (:playlistId IS NULL OR q.id IN (
            SELECT quoteId FROM quote_playlist_cross_ref WHERE playlistId = :playlistId
        ))
        AND q.id != :excludeId
        ORDER BY RANDOM() LIMIT 1
        """
    )
    suspend fun randomId(playlistId: Int?, excludeId: Int = -1): Int?

    @Query(
        """
        SELECT q.* FROM quotes q
        INNER JOIN quote_playlist_cross_ref r ON q.id = r.quoteId
        WHERE r.playlistId = :playlistId
        ORDER BY q.id DESC
        """
    )
    fun getInPlaylist(playlistId: Int): Flow<List<Quote>>

    @Insert
    suspend fun insert(quote: Quote): Long

    @Update
    suspend fun update(quote: Quote)

    @Delete
    suspend fun delete(quote: Quote)
}
