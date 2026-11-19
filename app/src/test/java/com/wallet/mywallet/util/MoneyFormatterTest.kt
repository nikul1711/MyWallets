package com.wallet.mywallet.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun parse_convertsTypedTextToCents() {
        assertEquals(1250L, MoneyFormatter.parse("12.50", "USD"))
        assertEquals(1200L, MoneyFormatter.parse("12", "USD"))
        assertEquals(5L, MoneyFormatter.parse("0.05", "USD"))
    }

    @Test
    fun parse_acceptsCommaAsDecimalSeparator() {
        assertEquals(1250L, MoneyFormatter.parse("12,50", "EUR"))
    }

    @Test
    fun parse_roundsHalfUp() {
        assertEquals(101L, MoneyFormatter.parse("1.005", "USD"))
    }

    @Test
    fun parse_allowsNegativeNumbers() {
        assertEquals(-500L, MoneyFormatter.parse("-5", "USD"))
    }

    @Test
    fun parse_usesZeroDecimalsForYen() {
        assertEquals(500L, MoneyFormatter.parse("500", "JPY"))
    }

    @Test
    fun parse_rejectsInvalidText() {
        assertNull(MoneyFormatter.parse("", "USD"))
        assertNull(MoneyFormatter.parse("   ", "USD"))
        assertNull(MoneyFormatter.parse("abc", "USD"))
        assertNull(MoneyFormatter.parse("-", "USD"))
        assertNull(MoneyFormatter.parse(null, "USD"))
    }

    @Test
    fun parse_doesNotLoseCentsToFloatingPointErrors() {
        // 0.1 + 0.2 is 0.30000000000000004 as a Double; as cents it is exact.
        val sum = MoneyFormatter.parse("0.10", "USD")!! + MoneyFormatter.parse("0.20", "USD")!!
        assertEquals(30L, sum)
    }

    @Test
    fun toPlain_isTheInverseOfParse() {
        assertEquals("12.50", MoneyFormatter.toPlain(1250, "USD"))
        assertEquals("-0.05", MoneyFormatter.toPlain(-5, "USD"))
        assertEquals("500", MoneyFormatter.toPlain(500, "JPY"))
    }

    @Test
    fun fractionDigits_fallsBackToTwoForUnknownCodes() {
        assertEquals(2, MoneyFormatter.fractionDigits("NOT_A_CODE"))
    }
}
