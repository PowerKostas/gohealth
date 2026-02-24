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
import com.example.gohealth.data.viewModels.ThemeViewModel

// Gets the themeViewModel to update the state of the theme option
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(themeViewModel: ThemeViewModel = viewModel()) {
    val scrollState = rememberScrollState()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)) {
        ProfilePicture()

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
                var username by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu("Gender", listOf("Male", "Female"))

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField("Age", 150f)

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField("Height (cm)", 300f)

                Spacer(modifier = Modifier.height(24.dp))

                NumberTextField("Weight (kg)", 700f)

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu("Activity Level", listOf("Sedentary", "Moderate", "High"))

                Spacer(modifier = Modifier.height(24.dp))

                DropdownMenu("Weight Goal", listOf("Lose", "Maintain", "Gain"))
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
            RadioButtonGroup(listOf("Light", "Dark", "Dynamic"), themeViewModel.selectedTheme) {
                newTheme -> themeViewModel.update(newTheme)
            }
        }
    }
}
