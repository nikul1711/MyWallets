package com.wallet.mywallet.billing

import androidx.annotation.StringRes
import com.wallet.mywallet.R

/**
 * Everything that differs between the free and the premium plan.
 *
 * To make a feature free or paid, change [freeLimit] here and nowhere else:
 *  - a number like 2 means "free users can have up to 2"
 *  - 0 means "premium only"
 * Premium users have no limit on anything.
 */
enum class Feature(
    @StringRes val titleRes: Int,
    @StringRes val freeTextRes: Int,
    @StringRes val premiumTextRes: Int,
    val freeLimit: Int
) {
    WALLETS(R.string.feature_wallets, R.string.feature_wallets_free, R.string.feature_wallets_premium, freeLimit = 2),
    BUDGETS(R.string.feature_budgets, R.string.feature_budgets_free, R.string.feature_budgets_premium, freeLimit = 2),
    GOALS(R.string.feature_goals, R.string.feature_goals_free, R.string.feature_goals_premium, freeLimit = 1),
    RECURRING_BILLS(R.string.feature_bills, R.string.feature_bills_free, R.string.feature_bills_premium, freeLimit = 0),
    ADVANCED_REPORTS(R.string.feature_reports, R.string.feature_reports_free, R.string.feature_reports_premium, freeLimit = 0)
}
