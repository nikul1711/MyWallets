package com.wallet.mywallet.billing

import com.wallet.mywallet.db.entity.PlanType
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.db.entity.SubscriptionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureAccessTest {

    @Test
    fun freeUser_canAddUpToTheFreeLimit() {
        assertTrue(FeatureAccess.canAdd(Feature.WALLETS, currentCount = 0, isPremium = false))
        assertTrue(FeatureAccess.canAdd(Feature.WALLETS, currentCount = 1, isPremium = false))
        assertFalse(FeatureAccess.canAdd(Feature.WALLETS, currentCount = 2, isPremium = false))
        assertFalse(FeatureAccess.canAdd(Feature.GOALS, currentCount = 1, isPremium = false))
    }

    @Test
    fun premiumUser_hasNoLimit() {
        assertTrue(FeatureAccess.canAdd(Feature.WALLETS, currentCount = 500, isPremium = true))
        assertTrue(FeatureAccess.canAdd(Feature.BUDGETS, currentCount = 500, isPremium = true))
    }

    @Test
    fun premiumOnlyFeatures_areBlockedForFreeUsers() {
        assertFalse(FeatureAccess.canUse(Feature.RECURRING_BILLS, isPremium = false))
        assertFalse(FeatureAccess.canUse(Feature.ADVANCED_REPORTS, isPremium = false))
        assertTrue(FeatureAccess.canUse(Feature.RECURRING_BILLS, isPremium = true))
        assertFalse(FeatureAccess.canAdd(Feature.RECURRING_BILLS, currentCount = 0, isPremium = false))
    }

    private fun subscription(plan: PlanType, status: SubscriptionStatus, expiresAt: Long?) =
        SubscriptionEntity(plan = plan, status = status, startedAt = 0, expiresAt = expiresAt, updatedAt = 0)

    @Test
    fun subscription_freePlanIsNeverPremium() {
        assertFalse(subscription(PlanType.FREE, SubscriptionStatus.ACTIVE, null).isPremium(now = 1_000))
    }

    @Test
    fun subscription_activePlanIsPremiumUntilItExpires() {
        val monthly = subscription(PlanType.PREMIUM_MONTHLY, SubscriptionStatus.ACTIVE, expiresAt = 2_000)
        assertTrue(monthly.isPremium(now = 1_999))
        assertFalse(monthly.isPremium(now = 2_000))
    }

    @Test
    fun subscription_lifetimeNeverExpires() {
        assertTrue(subscription(PlanType.LIFETIME, SubscriptionStatus.ACTIVE, null).isPremium(now = Long.MAX_VALUE))
    }

    @Test
    fun subscription_canceledOrExpiredStatusIsNotPremium() {
        assertFalse(subscription(PlanType.PREMIUM_YEARLY, SubscriptionStatus.CANCELED, null).isPremium(now = 1))
        assertFalse(subscription(PlanType.PREMIUM_YEARLY, SubscriptionStatus.EXPIRED, null).isPremium(now = 1))
        assertTrue(subscription(PlanType.PREMIUM_YEARLY, SubscriptionStatus.GRACE_PERIOD, null).isPremium(now = 1))
    }
}
