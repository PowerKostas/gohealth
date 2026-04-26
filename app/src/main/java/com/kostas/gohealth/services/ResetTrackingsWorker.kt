package com.kostas.gohealth.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kostas.gohealth.data.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

// Resets the tracking table if today is after the last reset date and a specific time has passed, the second part is just for
// testing, since I just want it to reset at midnight
class ResetTrackingsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)
                val trackingsDao = database.trackingsDao()
                val userTrackings = trackingsDao.getAll().first().firstOrNull()

                val settingsDao = database.settingsDao()
                val userSettings = settingsDao.getAll().first().firstOrNull()

                if (userTrackings != null && userSettings != null) {
                    if (LocalDate.now() > userSettings.lastResetDate && LocalTime.now() >= LocalTime.of(0, 0)) {

                        val resetTrackings = userTrackings.copy(
                            waterProgress = emptyList(),
                            caloriesProgress = emptyList(),
                            pushUpsProgress = emptyList(),
                            stepsProgress = 0
                        )

                        trackingsDao.update(resetTrackings)
                        settingsDao.update(userSettings.copy(lastResetDate = LocalDate.now()))
                    }
                }

                Result.success()
            }

            catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}
