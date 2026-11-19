package com.wallet.mywallet.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wallet.mywallet.db.dao.BudgetDao
import com.wallet.mywallet.db.dao.CategoryDao
import com.wallet.mywallet.db.dao.GoalDao
import com.wallet.mywallet.db.dao.RecurringBillDao
import com.wallet.mywallet.db.dao.SubscriptionDao
import com.wallet.mywallet.db.dao.TransactionDao
import com.wallet.mywallet.db.dao.UserDao
import com.wallet.mywallet.db.dao.WalletDao
import com.wallet.mywallet.db.entity.BudgetEntity
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.GoalEntity
import com.wallet.mywallet.db.entity.RecurringBillEntity
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.db.entity.TransactionEntity
import com.wallet.mywallet.db.entity.UserEntity
import com.wallet.mywallet.db.entity.WalletEntity

/**
 * The single local SQLite database of the app.
 *
 * Adding a table or column later? Raise [version] and add a Migration in [create].
 * We never use "fallbackToDestructiveMigration", because that would delete the user's money data.
 */
@Database(
    entities = [
        UserEntity::class,
        SubscriptionEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        RecurringBillEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringBillDao(): RecurringBillDao

    companion object {
        private const val DATABASE_NAME = "mywallet.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
