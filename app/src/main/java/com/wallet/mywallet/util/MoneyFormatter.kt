package com.wallet.mywallet.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Converts between what the user types ("12.50") and what the database stores (1250 cents).
 * All money in the app goes through this one class.
 */
object MoneyFormatter {

    /** How many decimals the currency has: 2 for USD, 0 for JPY. */
    fun fractionDigits(currencyCode: String): Int =
        try {
            Currency.getInstance(currencyCode).defaultFractionDigits.let { if (it < 0) 2 else it }
        } catch (e: IllegalArgumentException) {
            2
        }

    /** Turns typed text into minor units. Returns null when the text is not a valid number. */
    fun parse(text: String?, currencyCode: String): Long? {
        val cleaned = text?.trim()?.replace(',', '.') ?: return null
        if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return null
        return try {
            BigDecimal(cleaned)
                .movePointRight(fractionDigits(currencyCode))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (e: NumberFormatException) {
            null
        } catch (e: ArithmeticException) {
            null
        }
    }

    /** Minor units to plain text for an input field, for example 1250 -> "12.50". */
    fun toPlain(minor: Long, currencyCode: String): String =
        BigDecimal.valueOf(minor, fractionDigits(currencyCode)).toPlainString()

    /** Minor units to display text using the phone's number style, for example "$12.50". */
    fun format(minor: Long, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        try {
            formatter.currency = Currency.getInstance(currencyCode)
        } catch (e: IllegalArgumentException) {
            // Unknown code: keep the phone's default currency symbol.
        }
        return formatter.format(BigDecimal.valueOf(minor, fractionDigits(currencyCode)))
    }
}
