package com.example.gohealth

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gohealth.ui.components.central.DrawerMenu
import com.example.gohealth.ui.themes.GoHealthTheme
import com.example.gohealth.ui.viewModels.ThemeViewModel
import com.example.gohealth.ui.viewModels.UsersViewModel

// This is where the program starts, sets basic settings and runs the custom drawer menu function, which is the center of the app
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val usersViewModel: UsersViewModel = viewModel(factory = UsersViewModel.Factory)

            val usersList by usersViewModel.users.collectAsState()
            val currentUser = usersList.firstOrNull()

            var initialLoadDone by remember { mutableStateOf(false) }

            // When the app first opens, it initializes the theme with the value from the database
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    if (currentUser.appearance.isNotEmpty()) { // The first time the app opens, it uses the hard-coded Light mode
                        themeViewModel.update(currentUser.appearance)
                    }

                    initialLoadDone = true
                }
            }

            if (initialLoadDone) {
                // Gets the set theme option and passes it to the function that sets the theme
                val isDarkTheme = when (themeViewModel.selectedTheme) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }

                val useDynamicColor = themeViewModel.selectedTheme == "Dynamic"

                GoHealthTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColor) {
                    DrawerMenu()
                }
            }
        }
    }
}
