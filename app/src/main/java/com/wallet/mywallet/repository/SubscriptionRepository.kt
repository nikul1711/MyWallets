package com.wallet.mywallet.repository

import com.wallet.mywallet.billing.PurchasePlan
import com.wallet.mywallet.billing.PurchasePlans
import com.wallet.mywallet.db.dao.SubscriptionDao
import com.wallet.mywallet.db.entity.PlanType
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.db.entity.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    fun observeCurrent(): Flow<SubscriptionEntity?> = subscriptionDao.observeCurrent()

    /** Emits true while the user has an active premium plan. A missing row counts as free. */
    fun observeIsPremium(): Flow<Boolean> =
        subscriptionDao.observeCurrent().map { it?.isPremium() == true }

    suspend fun isPremium(): Boolean = subscriptionDao.getCurrent()?.isPremium() == true

    /**
     * TEST MODE ONLY: switches the plan on this phone without any payment.
     * When Google Play Billing is added, replace the call to this function with the real
     * purchase flow, and save the result with the real product id and purchase token.
     */
    suspend fun activatePlanForTesting(plan: PurchasePlan) {
        val now = System.currentTimeMillis()
        subscriptionDao.insert(
            SubscriptionEntity(
                plan = plan.type,
                status = SubscriptionStatus.ACTIVE,
                productId = plan.productId,
                startedAt = now,
                expiresAt = plan.durationDays?.let { now + TimeUnit.DAYS.toMillis(it.toLong()) },
                autoRenewing = plan.type == PlanType.PREMIUM_MONTHLY || plan.type == PlanType.PREMIUM_YEARLY,
                updatedAt = now
            )
        )
    }

    /** TEST MODE ONLY: go back to the free plan. */
    suspend fun resetToFree() = activatePlanForTesting(PurchasePlans.free)
}
