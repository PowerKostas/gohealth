package com.kostas.gohealth.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kostas.gohealth.MainActivity
import com.kostas.gohealth.R
import java.time.LocalTime

private const val CHANNEL_ID = "periodic_channel"
private val uniqueNotificationId = System.currentTimeMillis().toInt()

private val randomTitles = arrayOf("Daily Progress", "Push-ups Goal", "Push-ups Break", "Push-ups Reminder", "New Session")
private val randomTexts = arrayOf("Time for your push-up set ⏰", "Stay on track with a quick set of reps \uD83D\uDCAA", "A short set of push-ups will maintain your momentum ⚡", "Ready for your next set?", "Your future self will thank you!")

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    // Only sends notifications between 12pm and 12am
    override suspend fun doWork(): Result {
        if (LocalTime.now() >= LocalTime.of(12, 0)) {
            sendNotification()
        }

        return Result.success()
    }

    // Builds high importance stackable notifications
    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, "Periodic Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent? = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(randomTitles.random())
            .setContentText(randomTexts.random())
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(uniqueNotificationId, notification)
    }
}
