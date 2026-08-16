package xyz.crt572.quotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// v1 schema already contains playlists + per-quote style overrides — no migrations by design.
@Database(
    entities = [Quote::class, Playlist::class, QuotePlaylistCrossRef::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(QuoteStyleConverters::class)
abstract class QuoteDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var instance: QuoteDatabase? = null

        fun get(context: Context): QuoteDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quotes.db",
                ).build().also { instance = it }
            }
    }
}
