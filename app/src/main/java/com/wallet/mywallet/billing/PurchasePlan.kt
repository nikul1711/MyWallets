package com.wallet.mywallet.billing

import androidx.annotation.StringRes
import com.wallet.mywallet.R
import com.wallet.mywallet.db.entity.PlanType

/**
 * One plan the user can choose on the Premium screen.
 *
 * This is the catalogue of plans, kept in code. It is not stored in the database.
 * Prices are placeholders: when Google Play Billing is added, the real localized prices come
 * from Google Play ("ProductDetails") and [productId] is the id you create in Play Console.
 */
data class PurchasePlan(
    val type: PlanType,
    /** Google Play product id. Null for the free plan. */
    val productId: String?,
    @StringRes val titleRes: Int,
    @StringRes val priceRes: Int,
    @StringRes val periodRes: Int,
    /** How long one purchase lasts, in days. Null means forever. */
    val durationDays: Int?
)

object PurchasePlans {

    val free = PurchasePlan(
        type = PlanType.FREE,
        productId = null,
        titleRes = R.string.plan_free,
        priceRes = R.string.price_free,
        periodRes = R.string.period_forever,
        durationDays = null
    )

    val monthly = PurchasePlan(
        type = PlanType.PREMIUM_MONTHLY,
        productId = "mywallet_premium_monthly",
        titleRes = R.string.plan_monthly,
        priceRes = R.string.price_monthly,
        periodRes = R.string.period_month,
        durationDays = 30
    )

    val yearly = PurchasePlan(
        type = PlanType.PREMIUM_YEARLY,
        productId = "mywallet_premium_yearly",
        titleRes = R.string.plan_yearly,
        priceRes = R.string.price_yearly,
        periodRes = R.string.period_year,
        durationDays = 365
    )

    val lifetime = PurchasePlan(
        type = PlanType.LIFETIME,
        productId = "mywallet_premium_lifetime",
        titleRes = R.string.plan_lifetime,
        priceRes = R.string.price_lifetime,
        periodRes = R.string.period_once,
        durationDays = null
    )

    /** The plans that can be bought, in display order. */
    val purchasable = listOf(monthly, yearly, lifetime)

    fun of(type: PlanType): PurchasePlan =
        when (type) {
            PlanType.FREE -> free
            PlanType.PREMIUM_MONTHLY -> monthly
            PlanType.PREMIUM_YEARLY -> yearly
            PlanType.LIFETIME -> lifetime
        }
}
