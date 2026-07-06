package com.healthterra.ui.components.general

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.healthterra.R
import com.healthterra.ui.themes.LocalDarkTheme

@Composable
fun GoogleSignInButton(modifier: Modifier = Modifier, error: Boolean, onSignInClick: () -> Unit, onSignOutClick: () -> Unit) {
    // Listens to Firebase auth changes and when the uid changes, the button redraws, also uses an integer to force recompositions when a new
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

    val tempCurrentTrigger = authStateTrigger // It's needed for recomposition to happen

    val currentUser = Firebase.auth.currentUser

    val signedInWithGoogle = currentUser?.isAnonymous == false

    // Includes the user's profile picture and email
    val googleData = currentUser?.providerData?.find {
        it.providerId == GoogleAuthProvider.PROVIDER_ID
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (signedInWithGoogle) {
            Column {
                Text(
                    text = "Google Account",
                    modifier = modifier
                )

                CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = googleData?.photoUrl ?: R.drawable.anonymous,
                                contentDescription = "Google Profile Picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Signed in as",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = googleData?.email ?: "Unknown Email",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onSignOutClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Sign Out")
                        }
                    }
                }
            }
        }

        else {
            val isDark = LocalDarkTheme.current

            val backgroundColor = if (isDark) Color(0xFF131314) else Color(0xFFFFFFFF)
            val textColor = if (isDark) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)
            val borderStroke = if (isDark) BorderStroke(1.dp, Color(0xFF8E918F)) else BorderStroke(1.dp, Color(0xFF747775))

            Column {
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    onClick = onSignInClick,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),

                    shape = RoundedCornerShape(50),
                    color = backgroundColor,
                    border = borderStroke
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google_g_icon),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Sign in with Google",
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        )
                    }
                }
            }
        }

        Text(
            text = "An error occurred. Please try again later.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFE53935),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(if (error) 1f else 0f)
        )
    }
}
