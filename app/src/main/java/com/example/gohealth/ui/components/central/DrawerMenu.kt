package com.example.gohealth.ui.components.central

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gohealth.R
import com.example.gohealth.ui.screens.ProfileScreen
import kotlinx.coroutines.launch

// Drawer menu is the central screen of the app. It has the Scaffold, the top bar that remains static and is in charge of switching the
// different screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenu() {
    // drawerState is used to handle the opening and closing of the menu, scope is for the opening and closing animation, whenever the value
    // of currentScreen changes, Compose automatically reruns every line in this code that has currentScreen in it. Because this is the
    // central screen, this is where the main focus manager goes
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("Home") } // When the app opens, this is the screen that shows up
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {
            ModalDrawerSheet (
                drawerContainerColor = MaterialTheme.colorScheme.surface,

                // Make the drawer menu take 70% of the screen
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color(0xFF55403E))) { append("Go") }
                            withStyle(style = SpanStyle(color = Color(0xFF059669))) { append("Health") }
                        },

                        fontSize = 28.sp,
                        fontFamily = FontFamily(Font(R.font.fredoka_bold)),
                        modifier = Modifier.padding(16.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

                    DrawerMenuItem(
                        appIcon = R.drawable.home,
                        title = "Home",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.water,
                        title = "Water",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.calories,
                        title = "Calories",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.push_ups,
                        title = "Push-ups",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.steps,
                        title = "Steps",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.profile,
                        title = "Profile",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.leaderboards,
                        title = "Leaderboards",
                        currentScreen = currentScreen,
                        onItemClick = { selectedTitle ->
                            currentScreen = selectedTitle
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() })},

        topBar = {
            TopBar(
                title = currentScreen,
                onMenuClick = {
                    focusManager.clearFocus()
                    scope.launch { drawerState.open() }
                }
            )
        }) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                when (currentScreen) {
                    "Home" -> Text("This is the Home screen")
                    "Water" -> Text("This is the Water Intake screen")
                    "Calories" -> Text("This is the Calories Intake screen")
                    "Push-ups" -> Text("This is the Push-ups screen")
                    "Steps" -> Text("This is the Steps screen")
                    "Profile" -> ProfileScreen()
                    "Leaderboards" -> Text("This is the Leaderboards screen")
                }
            }
        }
    }
}
