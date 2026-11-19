package com.wallet.mywallet.billing

/**
 * The one place that answers "is the user allowed to do this?".
 * ViewModels call it before creating things, so screens never contain plan checks.
 */
object FeatureAccess {

    /** Can the user create one more item, given how many they already have? */
    fun canAdd(feature: Feature, currentCount: Int, isPremium: Boolean): Boolean =
        isPremium || currentCount < feature.freeLimit

    /** Can the user use a feature that is premium-only (or has a free limit above zero)? */
    fun canUse(feature: Feature, isPremium: Boolean): Boolean =
        isPremium || feature.freeLimit > 0
}
