package com.wallet.mywallet.db.model

import androidx.room.Embedded
import com.wallet.mywallet.db.entity.TransactionEntity

/**
 * Results of queries that join several tables (or group rows).
 * They are not tables themselves; Room fills them from the column names of each query.
 */

/** A transaction plus the names needed to show it in a list. */
data class TransactionDetail(
    @Embedded val transaction: TransactionEntity,
    val categoryName: String?,
    val walletName: String,
    val walletCurrency: String
)

/** Income and expense totals for one currency. */
data class CurrencyTotal(
    val currency: String,
    val income: Long,
    val expense: Long
)

/** Total spent in one category. A null category means "uncategorised". */
data class CategoryTotal(
    val categoryId: Long?,
    val categoryName: String?,
    val total: Long
)

/** Income and expense totals for one month. [month] looks like "2026-11". */
data class MonthlyTotal(
    val month: String,
    val income: Long,
    val expense: Long
)
