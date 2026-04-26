package com.kostas.gohealth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kostas.gohealth.services.LeaderboardSyncWorker
import com.kostas.gohealth.services.NotificationWorker
import com.kostas.gohealth.services.ResetTrackingsWorker
import com.kostas.gohealth.services.StepTrackerService
import com.kostas.gohealth.ui.components.central.DrawerMenu
import com.kostas.gohealth.ui.themes.GoHealthTheme
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.SettingsViewModel
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// This is where the program starts, sets basic settings and runs the custom drawer menu function, which is the center of the app
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    // On first time open, the code doesn't wait for user input on the permissions dialog and the foreground service doesn't have the
    // permissions to run, to fix this, foreground service runs from here too
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activityRecognitionGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
            if (activityRecognitionGranted) {
                val serviceIntent = Intent(this, StepTrackerService::class.java)
                startForegroundService(serviceIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Asks user for activity recognition and notifications permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACTIVITY_RECOGNITION))
        }

        // Asks user only for activity recognition permissions, notifications permissions are enabled by default in this version
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
        }

        schedulePeriodicNotification()
        scheduleDailyLeaderboardSync()
        scheduleDailyTrackingsReset()

        // Starts the foreground step tracking service, only if the step tracking setting and the physical activity permissions are
        // enabled. Steps are only counted if the foreground service is active
        lifecycleScope.launch {
            settingsViewModel.settings.collect { userSettingsList ->
                val userSettings = userSettingsList.firstOrNull()
                if (userSettings?.stepTracking == "Enabled") {
                    val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                            startForegroundService(serviceIntent)
                        }
                    }

                    // Below this version, permissions are not needed
                    else {
                        startForegroundService(serviceIntent)
                    }
                }

                // Kills the service if the user disables the setting
                else if (userSettings?.stepTracking == "Disabled") {
                    val stopIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    stopService(stopIntent)
                }
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
            val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)

            val userSettingsList by settingsViewModel.settings.collectAsState()
            val userSettings = userSettingsList.firstOrNull()
            val userId = userSettings?.userId

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

    // Handles edge case where, with the app on the background, the user allows activity recognition permissions and reopens the app, this
    // opens the foreground service in that instance
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val userSettingsList = settingsViewModel.settings.first()
            val userSettings = userSettingsList.firstOrNull()

            if (userSettings?.stepTracking == "Enabled") {
                val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                        startForegroundService(serviceIntent)
                    }
                }

                else {
                    startForegroundService(serviceIntent)
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

    // Updates the remote Firestore database every midnight, needs network
    private fun scheduleDailyLeaderboardSync() {
        // Testing
        //val testRequest = OneTimeWorkRequestBuilder<DailySyncWorker>().build()
        //WorkManager.getInstance(this).enqueue(testRequest)

        //val now = LocalDateTime.now()
        //val nextMidnight = LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay()
        //val delayInMilliseconds = Duration.between(now, nextMidnight).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<LeaderboardSyncWorker>(15, TimeUnit.MINUTES)
            //.setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )

            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // Resets the trackings table every midnight, doesn't need network
    private fun scheduleDailyTrackingsReset() {
        // Sets an initial delay to sync the 24-hour timer to midnight
        //val now = LocalDateTime.now()
        //val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        //val delayInMilliseconds = Duration.between(now, nextMidnight).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<ResetTrackingsWorker>(15, TimeUnit.MINUTES)
            //.setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )

            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
