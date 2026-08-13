package com.healthterra.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.generateRandomUsername
import com.healthterra.services.FirebaseDeleteWorker
import com.healthterra.services.RoomDeleteWorker
import com.healthterra.services.linkWithGoogleSignIn
import com.healthterra.services.syncAllTrackingsToFirestore
import com.healthterra.services.syncFirestoreUserToRoom
import com.healthterra.ui.components.general.ActionButton
import com.healthterra.ui.components.general.CustomDropdownMenu
import com.healthterra.ui.components.general.CustomSurface
import com.healthterra.ui.components.general.GoogleSignInButton
import com.healthterra.ui.components.general.InfoDialog
import com.healthterra.ui.components.general.NumberTextField
import com.healthterra.ui.components.general.RadioButtonGroup
import com.healthterra.ui.components.screen.ProfilePicture
import com.healthterra.ui.components.screen.WeightGoalSelector
import com.healthterra.ui.viewModels.AchievementsViewModel
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TodayTrackingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    // Uses the same instance of SettingsViewModel as MainActivity to fix bug on non-essential user settings Firestore syncing
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity

    val settingsViewModel: SettingsViewModel = viewModel(viewModelStoreOwner = activity, factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull()

    val todayTrackingsViewModel: TodayTrackingsViewModel = viewModel(factory = TodayTrackingsViewModel.Factory)
    val userTodayTrackingsList by todayTrackingsViewModel.todayTrackings.collectAsState()
    val userTodayTrackings = userTodayTrackingsList.firstOrNull()

    val achievementsViewModel: AchievementsViewModel = viewModel(factory = AchievementsViewModel.Factory)
    val userAchievementsList by achievementsViewModel.leaderboardsAchievements.collectAsState()
    val userAchievements = userAchievementsList.firstOrNull()

    // Waits for the database to load
    if (userCharacteristics == null || userSettings == null || userTodayTrackings == null || userAchievements == null) {
        return
    }

    val formatNumber = { num: Float? ->
        when {
            num == null -> ""
            num % 1 == 0f -> num.toInt().toString()
            else -> num.toString()
        }
    }

    // Initializes the variables that are used in text fields with the values from the database
    var username by remember(userSettings.username) { mutableStateOf(userSettings.username) }
    var age by remember(userCharacteristics.age) { mutableStateOf(formatNumber(userCharacteristics.age)) }
    var height by remember(userCharacteristics.height) { mutableStateOf(formatNumber(userCharacteristics.height)) }
    var weight by remember(userCharacteristics.weight) { mutableStateOf(formatNumber(userCharacteristics.weight)) }

    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Listens to Firebase auth changes and when the uid changes, the uid text redraws, also uses an integer to force recompositions when a new
    // Google account is created and the uid stays the same
    var authStateTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.IdTokenListener {
            authStateTrigger += 1
        }

        Firebase.auth.addIdTokenListener(authListener)

        onDispose {
            Firebase.auth.removeIdTokenListener(authListener)
        }
    }

    val tempAuthStateTrigger = authStateTrigger // It's needed for recomposition to happen

    val currentUser = Firebase.auth.currentUser
    val uidText = currentUser?.uid ?: "None"

    var isGoogleSignInLoading by rememberSaveable { mutableStateOf(false) }
    var googleAuthError by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    var deleteWorkId by rememberSaveable { mutableStateOf<UUID?>(null) }

    // To disable the delete account button, if the heavy Firebase delete worker is already running or if the UID is already null
    val workInfo by remember(deleteWorkId) {
        deleteWorkId?.let { id ->
            WorkManager.getInstance(context).getWorkInfoByIdFlow(id)
        } ?: flowOf(null)
    }.collectAsState(initial = null)

    val isDeleteButtonEnabled = workInfo?.state != WorkInfo.State.RUNNING && workInfo?.state != WorkInfo.State.ENQUEUED && currentUser != null


    // Draws the screen
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
    ) {
        ProfilePicture(userSettings.profilePictureString) { newProfilePictureString ->
            // Function that triggers when a new profile picture is tapped, it makes sure that a user is actually loaded on the screen, updates
            // the UI instantly, creates a copy of the user and only updates the profile picture String in the local database
            userSettings.let { settings ->
                settingsViewModel.updateUserSettings(settings.copy(profilePictureString = newProfilePictureString), context)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Personal Details",
            modifier = Modifier.align(Alignment.Start)
        )

        CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                val isError = username.length < 5
                OutlinedTextField(
                    value = username,
                    label = { Text("Username") },
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                // Local database update
                                if (username.length >= 5) {
                                    settingsViewModel.updateUserSettings(userSettings.copy(username = username), context)
                                }
                            }
                        },

                    // Adds an error/counting text below the field
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isError) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.offset(x = (-16).dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error Icon",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Text(text = "Minimum 5 characters")
                                }
                            }

                            // Empty space to push the counter to the right
                            else {
                                Text(text = "")
                            }

                            Text(
                                text = "${username.length} / 15",
                                textAlign = TextAlign.End,
                                modifier = Modifier.offset(x = 16.dp)
                            )
                        }
                    },

                    // Updates the UI, every time the text changes
                    onValueChange = { newValue ->
                        if (newValue.length <= 15) {
                            username = newValue
                        }
                    }
                )

                CustomDropdownMenu(
                    "Gender", listOf("Male", "Female"),
                    userCharacteristics.gender ?: ""
                ) { newValue ->
                    userCharacteristics.let { characteristics ->
                        characteristicsViewModel.updateUserCharacteristics(characteristics.copy(gender = newValue), context)
                    }

                    focusManager.clearFocus() // Clears focus upon selection
                }

                NumberTextField(
                    "Age",
                    16f,
                    130f,
                    age,

                    onFocusLost = {
                        val ageValue = age.toFloatOrNull()
                        if (ageValue == null || ageValue >= 16f) {
                            userCharacteristics.let { characteristics ->
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(age = ageValue), context)
                            }
                        }
                    },

                    onValueChange = { newValue -> age = newValue }
                )

                NumberTextField(
                    "Height (cm)",
                    50f,
                    280f,
                    height,

                    onFocusLost = {
                        val heightValue = height.toFloatOrNull()
                        if (heightValue == null || heightValue >= 50f) {
                            userCharacteristics.let { characteristics ->
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(height = heightValue), context)
                            }
                        }
                    },

                    onValueChange = { newValue -> height = newValue }
                )

                NumberTextField(
                    "Weight (kg)",
                    20f,
                    700f,
                    weight,

                    onFocusLost = {
                        val weightValue = weight.toFloatOrNull()
                        if (weightValue == null || weightValue >= 20f) {
                            userCharacteristics.let { characteristics ->
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(weight = weightValue), context)
                            }
                        }
                    },

                    onValueChange = { newValue -> weight = newValue }
                )

                CustomDropdownMenu(
                    "Activity Level", listOf("Sedentary", "Moderate", "High"),
                    userCharacteristics.activityLevel ?: ""
                ) { newValue ->
                    userCharacteristics.let { characteristics ->
                        characteristicsViewModel.updateUserCharacteristics(characteristics.copy(activityLevel = newValue), context)
                    }

                    focusManager.clearFocus()
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Weight Goal")

                    WeightGoalSelector(
                        userCharacteristics,
                        userSettings
                    ) { newWeightGoal, newKgGoal, newDaysGoal ->
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(characteristics.copy(weightGoal = newWeightGoal, kgGoal = newKgGoal, daysGoal = newDaysGoal), context)

                            // Also updates initialWeightGoalDate so it starts the count again, it becomes null if maintain is selected
                            settingsViewModel.updateUserSettings(
                                if (newWeightGoal == "Maintain") {
                                    userSettings.copy(initialWeightGoalDate = null)
                                }

                                else {
                                    userSettings.copy(initialWeightGoalDate = LocalDate.now().toString())
                                },

                                context
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            modifier = Modifier.align(Alignment.Start)
        )

        CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        options = listOf("Light", "Dark", "Dynamic"),
                        selectedOption = userSettings.appearance
                    ) { newAppearance ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(appearance = newAppearance), context)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Leaderboards Visibility",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        options = listOf("Public", "Anonymous"),
                        selectedOption = userSettings.leaderboardsVisibility
                    ) { newSetting ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(leaderboardsVisibility = newSetting), context)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Step Tracking",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        options = listOf("Enabled", "Disabled"),
                        selectedOption = userSettings.stepTracking
                    ) { newSetting ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(stepTracking = newSetting), context)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GoogleSignInButton(
            modifier = Modifier.align(Alignment.Start),
            error = googleAuthError,

            onSignInClick = {
                if (isGoogleSignInLoading) return@GoogleSignInButton

                isGoogleSignInLoading = true
                googleAuthError = false

                coroutineScope.launch {
                    try {
                        val returnCode = linkWithGoogleSignIn(activity)

                        if (returnCode == 2 || returnCode == 3) {
                            settingsViewModel.setPendingAction(true)

                            syncFirestoreUserToRoom(UserDatabase.getDatabase(context), context)

                            // Adds an 1-second delay to make sure the Room operations are done
                            coroutineScope.launch {
                                delay(1.seconds)
                                settingsViewModel.setPendingAction(false)
                            }

                            val message = if (returnCode == 2) {
                                "Google account linked! Your progress is now synced."
                            }

                            else {
                                "Account found. Personal information restored, progress updated."
                            }

                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                            // Uploads the merged local daily and today trackings to Firestore, because that's the only table on the cloud that can
                            // change, don't have to do it on returnCode = 2 because the UID document stays the same
                            if (returnCode == 3) {
                                syncAllTrackingsToFirestore(UserDatabase.getDatabase(context))
                            }
                        }

                        else if (returnCode == 0) {
                            googleAuthError = true
                        }
                    }

                    finally {
                        isGoogleSignInLoading = false
                    }
                }
            },

            onSignOutClick = {
                googleAuthError = false
                showSignOutDialog = true
            }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment =  Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "UID: $uidText",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )

                val siteHomeUrl = "https://powerkostas.github.io/healthterra-web/"
                val uriHandler = LocalUriHandler.current
                Text(
                    text = "Support & Legal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0645AD),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable {
                            uriHandler.openUri(siteHomeUrl)
                        }
                )
            }

            ActionButton(
                colour = Color(0xFFE53935),
                text = "Delete Account",
                fontSize = 12.sp,
                enabled = isDeleteButtonEnabled
            ) {
                showDeleteDialog = true
            }
        }
    }


    if (showDeleteDialog) {
        InfoDialog(
            icon = Icons.Default.Error,
            iconColour = Color(0xFFE53935),
            title = null,
            text = AnnotatedString("Your account and all associated data will be permanently deleted. This action is irreversible. Are you sure you want to proceed?"),
            confirmText = "Confirm",
            dismissText = "Cancel",
            isCancelable = true,
            fontWeight = FontWeight.Bold,

            onConfirm = {
                showDeleteDialog = false
                val randomUsername = generateRandomUsername()

                // Cancels all other workers/syncs as to not resurrect the deleted user document
                val workManager = WorkManager.getInstance(context)
                workManager.cancelAllWorkByTag("SyncDailyTrackingsWorker_${LocalDate.now()}")
                workManager.cancelAllWorkByTag("SyncUserDailyTrackingsWorker_${LocalDate.now()}")
                workManager.cancelAllWorkByTag("SyncUserWorker")

                // Firebase delete, needs network
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

                val deleteRequest = OneTimeWorkRequestBuilder<FirebaseDeleteWorker>()
                    .setConstraints(constraints)
                    .build()

                // UI and local database delete
                val roomDeleteData = Data.Builder()
                    .putString("randomUsername", randomUsername)
                    .build()

                val roomDeleteRequest = OneTimeWorkRequestBuilder<RoomDeleteWorker>()
                    .setInputData(roomDeleteData)
                    .build()

                WorkManager.getInstance(context)
                    .beginWith(deleteRequest)
                    .then(roomDeleteRequest)
                    .enqueue()

                deleteWorkId = roomDeleteRequest.id

                coroutineScope.launch {
                    settingsViewModel.markSyncHandled()
                    settingsViewModel.setPendingAction(true)
                    delay(1.seconds)
                    settingsViewModel.setPendingAction(false)
                }
            },

            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showSignOutDialog) {
        InfoDialog(
            icon = Icons.Default.Error,
            iconColour = Color(0xFFFFA000),
            title = null,
            text = AnnotatedString("Your progress is backed up to this account. Do you want to sign out and continue as a guest?"),
            confirmText = "Confirm",
            dismissText = "Cancel",
            isCancelable = true,
            fontWeight = FontWeight.Bold,

            onConfirm = {
                showSignOutDialog = false
                val randomUsername = generateRandomUsername()

                // UI and local database delete
                val roomDeleteData = Data.Builder()
                    .putString("randomUsername", randomUsername)
                    .build()

                val roomDeleteRequest = OneTimeWorkRequestBuilder<RoomDeleteWorker>()
                    .setInputData(roomDeleteData)
                    .build()

                WorkManager.getInstance(context).enqueue(roomDeleteRequest)

                // Text fields UI delete
                username = randomUsername
                age = ""
                height = ""
                weight = ""

                coroutineScope.launch {
                    try {
                        settingsViewModel.setPendingAction(true)

                        // Signs out from Firestore and Firebase
                        val firestore = FirebaseFirestore.getInstance()
                        try {
                            firestore.waitForPendingWrites().await()
                            firestore.terminate().await()
                            firestore.clearPersistence().await()
                        }

                        catch (e: Exception) {
                            Log.e("Google sign-out", "Error terminating Firestore", e)
                        }

                        finally {
                            Firebase.auth.signOut()
                        }

                        // Signs out from Google
                        val credentialManager = CredentialManager.create(context)
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())

                        delay(1.seconds)
                        settingsViewModel.setPendingAction(false)
                    }

                    catch (e: Exception) {
                        Log.e("Google sign-in", "Failed to sign out", e)
                        googleAuthError = true
                        settingsViewModel.setPendingAction(false)
                    }
                }
            },

            onDismiss = { showSignOutDialog = false }
        )
    }
}
