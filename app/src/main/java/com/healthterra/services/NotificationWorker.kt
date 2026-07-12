package com.healthterra.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.healthterra.MainActivity
import com.healthterra.R
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateWaterGoal
import kotlinx.coroutines.flow.first
import java.time.LocalTime

private const val CHANNEL_ID = "periodic_channel"

private val exerciseTitles = arrayOf("Daily Exercise Progress", "Exercise Goal", "Exercise Break", "Exercise Reminder", "New Exercise Session")
private val exerciseTexts = arrayOf("Time for your exercise set ⏰", "Stay on track with a quick set of reps \uD83D\uDCAA", "A short set of exercises will help maintain your momentum ⚡", "Ready for your next set?", "Your future self will thank you!")

private val waterTitles = arrayOf("Hydration Check", "Water Break")
private val waterTexts = arrayOf("Don't forget to drink some water! \uD83D\uDCA7", "Keep your hydration levels up! \uD83C\uDF0A")

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    // Only sends notifications between 12pm and 12am and only if the user hasn't completed their goal
    override suspend fun doWork(): Result {
        val database = UserDatabase.getDatabase(applicationContext)
        val userTodayTrackings = database.todayTrackingsDao().getAll().first().firstOrNull()
        val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

        if (userTodayTrackings == null || userCharacteristics == null) {
            return Result.success()
        }

        if (LocalTime.now() >= LocalTime.of(12, 0)) {
            val needsExercise = userTodayTrackings.exerciseProgress.sum() < calculateExerciseGoal(userCharacteristics)
            val needsWater = userTodayTrackings.waterProgress.sum() < calculateWaterGoal(userCharacteristics)

            val notificationPool = mutableListOf<Pair<String, String>>()

            if (needsExercise) {
                notificationPool.addAll(exerciseTitles.zip(exerciseTexts)) // Zips the titles and texts into Pairs and adds them all
            }

            if (needsWater) {
                notificationPool.addAll(waterTitles.zip(waterTexts))
            }

            if (notificationPool.isNotEmpty()) {
                val (title, text) = notificationPool.random()
                sendNotification(title, text)
            }
        }

        return Result.success()
    }

    // Builds high importance stackable notifications, opens the app when tapped
    private fun sendNotification(title: String, text: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, "Periodic Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
