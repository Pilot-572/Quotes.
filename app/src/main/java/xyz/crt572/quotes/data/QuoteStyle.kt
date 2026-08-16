package xyz.crt572.quotes.data

import androidx.room.TypeConverter

enum class QuoteSize { S, M, L, XL }

fun QuoteSize.toSp(): Int = when (this) {
    QuoteSize.S -> 14
    QuoteSize.M -> 18
    QuoteSize.L -> 22
    QuoteSize.XL -> 28
}

enum class TextAlignment { Start, Center, End }

enum class FontFamilyChoice { Serif, SansSerif, Monospace }

// Enums stored as their names; unknown strings (from a future version's data) decay to null
// instead of crashing, which means "no override" — the safe default.
class QuoteStyleConverters {
    @TypeConverter fun quoteSizeToString(v: QuoteSize?): String? = v?.name
    @TypeConverter fun stringToQuoteSize(v: String?): QuoteSize? =
        v?.let { runCatching { QuoteSize.valueOf(it) }.getOrNull() }

    @TypeConverter fun alignmentToString(v: TextAlignment?): String? = v?.name
    @TypeConverter fun stringToAlignment(v: String?): TextAlignment? =
        v?.let { runCatching { TextAlignment.valueOf(it) }.getOrNull() }

    @TypeConverter fun fontFamilyToString(v: FontFamilyChoice?): String? = v?.name
    @TypeConverter fun stringToFontFamily(v: String?): FontFamilyChoice? =
        v?.let { runCatching { FontFamilyChoice.valueOf(it) }.getOrNull() }
}
