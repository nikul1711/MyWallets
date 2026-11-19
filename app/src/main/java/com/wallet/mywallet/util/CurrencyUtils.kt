package com.wallet.mywallet.util

import java.util.Currency
import java.util.Locale

/** Helpers for the currency drop-downs. */
object CurrencyUtils {

    /** A short list of widely used currencies, so the drop-down stays easy to scroll. */
    val common = listOf(
        "USD", "EUR", "GBP", "INR", "JPY", "CNY", "AUD", "CAD", "CHF", "SEK", "NOK", "DKK",
        "NZD", "SGD", "HKD", "KRW", "MXN", "BRL", "ZAR", "AED", "SAR", "TRY", "PLN", "THB",
        "IDR", "MYR", "PHP", "VND", "PKR", "BDT", "LKR", "NGN", "EGP", "KES"
    )

    /** The currency of the phone's region, or USD if it cannot be found. */
    fun deviceDefault(): String =
        try {
            Currency.getInstance(Locale.getDefault()).currencyCode
        } catch (e: Exception) {
            "USD"
        }

    /** The common list, plus [extra] at the top if it is not already in it. */
    fun options(extra: String? = null): List<String> =
        (listOfNotNull(extra) + common).distinct()

    /** For example "USD - US Dollar". */
    fun label(code: String): String =
        try {
            "$code - ${Currency.getInstance(code).displayName}"
        } catch (e: Exception) {
            code
        }
}
