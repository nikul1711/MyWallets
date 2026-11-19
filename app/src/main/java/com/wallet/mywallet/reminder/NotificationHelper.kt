package com.wallet.mywallet.reminder

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wallet.mywallet.R
import com.wallet.mywallet.db.model.BillDetail
import com.wallet.mywallet.ui.MainActivity
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

/** Creates the notification channel and shows bill reminders. */
object NotificationHelper {

    private const val CHANNEL_ID = "bill_reminders"

    /** Android 8+ requires every notification to belong to a channel. Safe to call repeatedly. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_bills_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.channel_bills_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** False when the user denied the notification permission (Android 13+) or turned them off. */
    fun canNotify(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    // We check canNotify() before calling this, so the permission warning is a false alarm.
    @SuppressLint("MissingPermission")
    fun showBillReminder(context: Context, detail: BillDetail) {
        val days = DateUtils.daysBetween(DateUtils.today(), detail.bill.nextDueDate)
        val message = when {
            days < 0 -> context.resources.getQuantityString(R.plurals.reminder_overdue_days, -days, -days)
            days == 0 -> context.getString(R.string.reminder_due_today)
            else -> context.resources.getQuantityString(R.plurals.reminder_due_in_days, days, days)
        }
        val amount = MoneyFormatter.format(detail.bill.amount, detail.walletCurrency)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${detail.bill.name} - $amount")
            .setContentText(message)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        // Using the bill id as the notification id means a bill never shows twice.
        NotificationManagerCompat.from(context).notify(detail.bill.id.toInt(), notification)
    }
}
