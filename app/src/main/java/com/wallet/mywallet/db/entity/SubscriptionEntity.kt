package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The user's current plan. The newest row is the active one.
 *
 * Today the rows are created locally (test mode). When Google Play Billing is added later,
 * `productId` and `purchaseToken` will come from the real purchase, and this table becomes a
 * cache of what Google Play says.
 */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = UserEntity.LOCAL_USER_ID,
    val plan: PlanType,
    val status: SubscriptionStatus,
    /** Google Play product id. Null while payments are not connected. */
    val productId: String? = null,
    /** Google Play purchase token. Null while payments are not connected. */
    val purchaseToken: String? = null,
    val startedAt: Long,
    /** Null means it never expires (FREE and LIFETIME). */
    val expiresAt: Long? = null,
    val autoRenewing: Boolean = false,
    val updatedAt: Long
) {
    /** True when the user should get premium features right now. */
    fun isPremium(now: Long = System.currentTimeMillis()): Boolean =
        plan != PlanType.FREE &&
            (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.GRACE_PERIOD) &&
            (expiresAt == null || expiresAt > now)
}
