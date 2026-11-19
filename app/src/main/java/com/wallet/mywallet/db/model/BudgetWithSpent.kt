package com.wallet.mywallet.db.model

import androidx.room.Embedded
import com.wallet.mywallet.db.entity.BudgetEntity

/** A budget plus how much has been spent against it in the current month. */
data class BudgetWithSpent(
    @Embedded val budget: BudgetEntity,
    /** Null when the budget covers all expenses. */
    val categoryName: String?,
    val spent: Long
)
