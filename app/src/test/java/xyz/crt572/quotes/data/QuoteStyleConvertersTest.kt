package xyz.crt572.quotes.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteStyleConvertersTest {
    private val c = QuoteStyleConverters()

    @Test
    fun roundTripsEveryEnumValue() {
        QuoteSize.entries.forEach { assertEquals(it, c.stringToQuoteSize(c.quoteSizeToString(it))) }
        TextAlignment.entries.forEach { assertEquals(it, c.stringToAlignment(c.alignmentToString(it))) }
        FontFamilyChoice.entries.forEach { assertEquals(it, c.stringToFontFamily(c.fontFamilyToString(it))) }
    }

    @Test
    fun unknownStringsDecayToNullInsteadOfCrashing() {
        assertNull(c.stringToQuoteSize("XXL"))
        assertNull(c.stringToAlignment("Justified"))
        assertNull(c.stringToFontFamily("ComicSans"))
    }

    @Test
    fun nullsPassThrough() {
        assertNull(c.quoteSizeToString(null))
        assertNull(c.stringToQuoteSize(null))
    }
}
