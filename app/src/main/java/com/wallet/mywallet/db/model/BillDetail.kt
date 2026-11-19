package com.wallet.mywallet.db.model

import androidx.room.Embedded
import com.wallet.mywallet.db.entity.RecurringBillEntity

/** A recurring bill plus the names needed to show it in a list. */
data class BillDetail(
    @Embedded val bill: RecurringBillEntity,
    val walletName: String,
    val walletCurrency: String,
    val categoryName: String?
)
