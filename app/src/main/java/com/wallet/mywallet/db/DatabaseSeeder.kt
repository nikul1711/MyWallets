package com.wallet.mywallet.db

import android.content.Context
import androidx.room.withTransaction
import com.wallet.mywallet.R
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.PlanType
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.db.entity.SubscriptionStatus
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.db.entity.UserEntity
import com.wallet.mywallet.util.CurrencyUtils

/**
 * Fills a brand-new database with the starting data: the local user profile,
 * the FREE plan and a list of default categories.
 * It is safe to call on every app start: it only adds what is missing.
 */
class DatabaseSeeder(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend fun seedIfNeeded() {
        db.withTransaction {
            val now = System.currentTimeMillis()

            if (db.userDao().get() == null) {
                db.userDao().upsert(
                    UserEntity(
                        displayName = context.getString(R.string.default_user_name),
                        defaultCurrency = CurrencyUtils.deviceDefault(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            if (db.subscriptionDao().getCurrent() == null) {
                db.subscriptionDao().insert(
                    SubscriptionEntity(
                        plan = PlanType.FREE,
                        status = SubscriptionStatus.ACTIVE,
                        startedAt = now,
                        updatedAt = now
                    )
                )
            }

            if (db.categoryDao().count() == 0) {
                val expense = context.resources.getStringArray(R.array.default_expense_categories)
                    .map { CategoryEntity(name = it, type = TransactionType.EXPENSE) }
                val income = context.resources.getStringArray(R.array.default_income_categories)
                    .map { CategoryEntity(name = it, type = TransactionType.INCOME) }
                db.categoryDao().insertAll(expense + income)
            }
        }
    }
}
