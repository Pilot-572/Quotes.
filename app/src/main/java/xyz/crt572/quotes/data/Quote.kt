package xyz.crt572.quotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Style override columns are in the schema from v1 — null means "follow widget prefs".
@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val author: String? = null,
    val source: String? = null,
    val sizeOverride: QuoteSize? = null,
    val alignmentOverride: TextAlignment? = null,
    val boldOverride: Boolean? = null,
    val italicOverride: Boolean? = null,
    val fontFamilyOverride: FontFamilyChoice? = null,
)
