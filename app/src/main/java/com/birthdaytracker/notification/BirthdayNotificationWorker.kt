package com.birthdaytracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.birthdaytracker.MainActivity
import com.birthdaytracker.R
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.repository.BirthdayRepository
import com.birthdaytracker.util.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period
import java.util.concurrent.TimeUnit

@HiltWorker
class BirthdayNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: BirthdayRepository,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting birthday notification check")
            checkAndNotifyBirthdays()
            Log.d(TAG, "Birthday notification check completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking birthdays for notifications", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun checkAndNotifyBirthdays() {
        val notificationDayOf = preferencesManager.notificationDayOf.first()
        val notificationWeekBefore = preferencesManager.notificationWeekBefore.first()

        val allBirthdays = repository.getAllBirthdays().first()
        val today = LocalDate.now()

        Log.d(TAG, "Checking ${allBirthdays.size} birthdays")

        allBirthdays.forEach { birthday ->
            try {
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear

                val daysUntil = Period.between(today, upcoming).days

                if ((daysUntil == 0 && notificationDayOf) ||
                    (daysUntil == 7 && notificationWeekBefore)) {
                    Log.d(TAG, "Showing notification for ${birthday.name}, days until: $daysUntil")
                    showNotification(applicationContext, birthday, daysUntil)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing birthday for ${birthday.name}", e)
            }
        }
    }

    private fun showNotification(context: Context, birthday: Birthday, daysUntil: Int) {
        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            birthday.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when (daysUntil) {
            0 -> context.getString(R.string.birthday_today, birthday.name)
            1 -> context.getString(R.string.birthday_tomorrow, birthday.name)
            else -> context.getString(R.string.birthday_week, birthday.name, daysUntil)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(birthday.id.toInt(), notification)
    }

    companion object {
        private const val TAG = "BirthdayNotificationWorker"
        private const val CHANNEL_ID = "birthday_reminders"

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        }

        fun scheduleNotifications(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // Schedule daily check for birthdays at 9 AM
            val dailyRequest = PeriodicWorkRequestBuilder<BirthdayNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "birthday_notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )

            Log.d(TAG, "Notification work scheduled")
        }

        private fun calculateInitialDelay(): Long {
            val now = LocalDate.now()
            val targetTime = now.atTime(9, 0) // 9 AM
            val currentTime = LocalDate.now().atStartOfDay()

            var delay = java.time.Duration.between(currentTime, targetTime).toMillis()
            if (delay < 0) {
                delay += TimeUnit.DAYS.toMillis(1)
            }
            return delay
        }
    }
}