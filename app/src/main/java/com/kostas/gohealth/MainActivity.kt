package com.kostas.gohealth

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kostas.gohealth.services.NotificationWorker
import com.kostas.gohealth.ui.components.central.DrawerMenu
import com.kostas.gohealth.ui.themes.GoHealthTheme
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.SettingsViewModel
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

// This is where the program starts, sets basic settings and runs the custom drawer menu function, which is the center of the app
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Asks user for activity recognition and notifications permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // Asks user only for activity recognition permissions, notifications permissions are enabled by default in this version
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
        }

        schedulePeriodicNotification()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
            val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)

            val userSettingsList by settingsViewModel.settings.collectAsState()
            val userSettings = userSettingsList.firstOrNull()
            val userId = userSettings?.userId

            val userTrackingsList by trackingsViewModel.trackings.collectAsState()
            val userTrackings = userTrackingsList.firstOrNull()

            // Settings is the table with the primary key, it's initialized automatically. LaunchedEffect runs everytime the key
            // changes, including the initialization to a null value, so the actual block here only executes the first time the user opens
            // the app. The other 2 tables, with the foreign keys, get initialized when that happens
            LaunchedEffect(userId) {
                if (userId != null) {
                    val userId = userSettingsList.first().userId
                    characteristicsViewModel.initializeUserCharacteristics(userId)
                    trackingsViewModel.initializeUserTrackings(userId)
                }
            }

            // The while loop executes when userTrackings and userSettings get initialized and stays true until the app is closed. The loop
            // is checking every minute if the day has changed to reset the trackings table
            LaunchedEffect(userTrackings != null && userSettings != null) {
                if (userTrackings != null && userSettings != null) {
                    while (isActive) {
                        trackingsViewModel.resetUserTrackings(userTrackings, userSettings)
                        delay(60000)
                    }
                }
            }

            // Gets the set theme option and passes it to the function that sets the theme
            if (userSettings != null) {
                val isDarkTheme = when (userSettings.appearance) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }

                val useDynamicColor = userSettings.appearance == "Dynamic"

                GoHealthTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColor) {
                    DrawerMenu()
                }
            }
        }
    }

    // Sends the already made notification every 3 hours, doesn't need network
    private fun schedulePeriodicNotification() {
        // Testing
        //val testRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        //WorkManager.getInstance(this).enqueue(testRequest)

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(3, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )

            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
