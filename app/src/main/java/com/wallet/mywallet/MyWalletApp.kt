package com.wallet.mywallet

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallet.mywallet.reminder.BillReminderWorker
import com.wallet.mywallet.reminder.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Runs once when the app process starts. */
class MyWalletApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Add the default profile, plan and categories on the very first launch.
        appScope.launch { container.seeder.seedIfNeeded() }

        NotificationHelper.createChannel(this)
        scheduleBillReminders()
    }

    /** Checks once a day for bills that are due soon. KEEP means "do not schedule it twice". */
    private fun scheduleBillReminders() {
        val request = PeriodicWorkRequestBuilder<BillReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("bill_reminders", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
