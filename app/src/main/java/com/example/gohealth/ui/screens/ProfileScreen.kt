package com.example.gohealth.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gohealth.ui.components.general.DropdownMenu
import com.example.gohealth.ui.components.general.NumberTextField
import com.example.gohealth.ui.components.general.RadioButtonGroup
import com.example.gohealth.ui.components.profile.ProfilePicture
import com.example.gohealth.ui.viewModels.ThemeViewModel
import com.example.gohealth.ui.viewModels.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    themeViewModel: ThemeViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel(factory = UsersViewModel.Factory)
) {
    val usersList by usersViewModel.users.collectAsState()
    val currentUser = usersList.firstOrNull()

    var profilePictureString by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("") }
    var weightGoal by remember { mutableStateOf("") }

    // Flag to stop the program from entering the LaunchedEffect function when typing in the text field
    var initialLoadDone by remember { mutableStateOf(false) }

    // When the app first opens it initializes the variables with the values from the database, also runs every time currentUser changes, but
    // that will probably not happen
    LaunchedEffect(currentUser) {
        if (currentUser != null && !initialLoadDone) {
            profilePictureString = currentUser.profilePictureString
            username = currentUser.username
            gender = currentUser.gender

            val formatNumber = { num: Float? ->
                when {
                    num == null -> "" // If DB is null, show blank
                    num % 1 == 0f -> num.toInt().toString() // If whole number, chop off .0
                    else -> num.toString() // If decimal, leave it alone
                }
            }

            age = formatNumber(currentUser.age)
            height = formatNumber(currentUser.height)
            weight = formatNumber(currentUser.weight)

            activityLevel = currentUser.activityLevel
            weightGoal = currentUser.weightGoal

            initialLoadDone = true
        }
    }

    // Blank loading screen
    if (!initialLoadDone ) {
        return
    }

    val scrollState = rememberScrollState()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)) {
        ProfilePicture(
            profilePictureString

        // Function that triggers when a new profile picture is tapped, it makes sure that a user is actually loaded on the screen, updates
        // the UI instantly, creates a copy of the user and only updates the profile picture String in the local database
        ) { newProfilePictureString ->
            profilePictureString = newProfilePictureString
            currentUser?.let { user ->
                usersViewModel.updateUser(
                    user.copy(profilePictureString = newProfilePictureString)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Personal Details",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = username,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),

                    // Updates the local database, every time the text changes
                    onValueChange = { newValue ->
                        username = newValue

                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(username = newValue)
                            usersViewModel.updateUser(updatedUser)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu(
                    "Gender", listOf("Male", "Female"),
                    gender
                ) { newValue ->
                    gender = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(gender = newValue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField(
                    "Age",
                    150f,
                    age
                ) { newValue ->
                    age = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(age = newValue.toFloatOrNull())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField(
                    "Height (cm)",
                    300f,
                    height
                ) { newValue ->
                    height = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(height = newValue.toFloatOrNull())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField(
                    "Weight (kg)",
                    700f,
                    weight
                ) { newValue ->
                    weight = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(weight = newValue.toFloatOrNull())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu(
                    "Activity Level", listOf("Sedentary", "Moderate", "High"),
                    activityLevel
                ) { newValue ->
                    activityLevel = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(activityLevel = newValue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu(
                    "Weight Goal", listOf("Lose", "Maintain", "Gain"),
                    weightGoal
                ) { newValue ->
                    weightGoal = newValue
                    currentUser?.let { user ->
                        usersViewModel.updateUser(
                            user.copy(weightGoal = newValue)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Appearance",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            RadioButtonGroup(
                listOf("Light", "Dark", "Dynamic"),
                themeViewModel.selectedTheme
            ) { newTheme ->
                themeViewModel.update(newTheme)

                currentUser?.let { user ->
                    usersViewModel.updateUser(
                        user.copy(appearance = newTheme)
                    )
                }
            }
        }
    }
}
