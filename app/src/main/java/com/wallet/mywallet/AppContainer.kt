package com.wallet.mywallet

import android.content.Context
import com.wallet.mywallet.db.AppDatabase
import com.wallet.mywallet.db.DatabaseSeeder
import com.wallet.mywallet.repository.BillRepository
import com.wallet.mywallet.repository.BudgetRepository
import com.wallet.mywallet.repository.CategoryRepository
import com.wallet.mywallet.repository.GoalRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.TransactionRepository
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.repository.WalletRepository

/**
 * Creates the database and the repositories exactly once and hands them to the ViewModels.
 * This is "manual dependency injection": plain Kotlin, no libraries, easy to follow.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.create(context)

    val seeder = DatabaseSeeder(context.applicationContext, database)

    val userRepository = UserRepository(database.userDao())
    val subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
    val walletRepository = WalletRepository(database.walletDao())
    val categoryRepository = CategoryRepository(database.categoryDao())
    val transactionRepository = TransactionRepository(database.transactionDao())
    val budgetRepository = BudgetRepository(database.budgetDao())
    val goalRepository = GoalRepository(database.goalDao())
    val billRepository = BillRepository(database)
}
