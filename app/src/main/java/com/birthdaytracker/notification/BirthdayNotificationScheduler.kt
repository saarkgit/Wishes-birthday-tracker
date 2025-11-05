package com.birthdaytracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.birthdaytracker.MainActivity
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.util.PreferencesManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period
import java.util.concurrent.TimeUnit

class BirthdayNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            checkAndNotifyBirthdays()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun checkAndNotifyBirthdays() {
        val database = BirthdayDatabase.getDatabase(applicationContext)
        val dao = database.birthdayDao()
        val preferencesManager = PreferencesManager(applicationContext)
        
        val notificationDayOf = preferencesManager.notificationDayOf.first()
        val notificationWeekBefore = preferencesManager.notificationWeekBefore.first()
        
        val allBirthdays = dao.getAllBirthdays().first()
        val today = LocalDate.now()
        
        allBirthdays.forEach { birthday ->
            val thisYear = birthday.birthDate.withYear(today.year)
            val nextYear = birthday.birthDate.withYear(today.year + 1)
            val upcoming = if (thisYear >= today) thisYear else nextYear
            
            val daysUntil = Period.between(today, upcoming).days
            
            if ((daysUntil == 0 && notificationDayOf) || 
                (daysUntil == 7 && notificationWeekBefore)) {
                showNotification(applicationContext, birthday, daysUntil)
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
            0 -> "${birthday.name}'s birthday is today!"
            1 -> "${birthday.name}'s birthday is tomorrow!"
            else -> "${birthday.name}'s birthday is in $daysUntil days!"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Don't forget to wish them a happy birthday!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(birthday.id.toInt(), notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "birthday_reminders"
        private const val CHANNEL_NAME = "Birthday Reminders"
        
        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming birthdays"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel)
            }
        }
        
        fun scheduleNotifications(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            // Schedule daily check for birthdays
            val dailyRequest = PeriodicWorkRequestBuilder<BirthdayNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                "birthday_notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )
        }
    }
}

