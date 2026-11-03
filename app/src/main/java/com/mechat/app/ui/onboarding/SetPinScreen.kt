package com.mechat.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.di.LockEntryPoint
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PinDots
import com.mechat.app.ui.lock.Keypad
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors

/** PIN setup (6–12 digits) during onboarding. PIN-only, no biometrics by design. */
@Composable
fun SetPinScreen(onDone: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    val context = LocalContext.current
    val lockManager = remember {
        EntryPointAccessors.fromApplication(context, LockEntryPoint::class.java).appLockManager()
    }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GlassCard {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Text(
                        text = if (confirm == null) "Choose a passcode" else "Repeat passcode",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.textPrimary,
                    )
                    PinDots(filledCount = (confirm ?: pin).length.coerceAtMost(6))
                    if (error != null) {
                        Text(error!!, color = tokens.alert, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Keypad(
                modifier = Modifier.padding(top = Spacing.lg),
                onDigit = { d ->
                    val current = confirm ?: pin
                    if (current.length < 12) {
                        val next = current + d.toString()
                        if (confirm == null) {
                            pin = next
                            if (next.length >= 6) confirm = ""
                        } else {
                            if (next == pin) {
                                lockManager.setPin(pin)
                                onDone()
                            } else {
                                error = "Passcodes do not match"
                                pin = ""
                                confirm = null
                            }
                        }
                    }
                },
                onBackspace = {
                    if (confirm != null) confirm = (confirm!!).dropLast(1)
                    else pin = pin.dropLast(1)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetPinScreenPreview() {
    MeChatTheme { SetPinScreen(onDone = {}) }
}
