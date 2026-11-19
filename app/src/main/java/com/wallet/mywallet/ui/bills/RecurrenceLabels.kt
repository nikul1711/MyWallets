package com.wallet.mywallet.ui.bills

import androidx.annotation.StringRes
import com.wallet.mywallet.R
import com.wallet.mywallet.db.entity.RecurrenceInterval

/** The text shown for each repeat interval. Kept out of the enum so the database layer has no UI code. */
@StringRes
fun RecurrenceInterval.labelRes(): Int = when (this) {
    RecurrenceInterval.DAILY -> R.string.interval_daily
    RecurrenceInterval.WEEKLY -> R.string.interval_weekly
    RecurrenceInterval.MONTHLY -> R.string.interval_monthly
    RecurrenceInterval.YEARLY -> R.string.interval_yearly
}
