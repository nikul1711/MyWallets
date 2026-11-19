package com.wallet.mywallet.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallet.mywallet.MyWalletApp

/**
 * Runs once a day in the background (scheduled by WorkManager, which survives app restarts
 * and reboots) and shows a notification for every bill that is due soon.
 * Recurring bills are a premium feature, so free users get no reminders.
 */
class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MyWalletApp).container

        if (!container.subscriptionRepository.isPremium()) return Result.success()
        if (!NotificationHelper.canNotify(applicationContext)) return Result.success()

        container.billRepository.getDueForReminder().forEach { detail ->
            NotificationHelper.showBillReminder(applicationContext, detail)
        }
        return Result.success()
    }
}
