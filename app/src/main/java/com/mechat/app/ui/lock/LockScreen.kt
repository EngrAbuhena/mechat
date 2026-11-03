package com.mechat.app.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.di.LockEntryPoint
import com.mechat.app.domain.lock.AppLockManager
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PinDots
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing
import dagger.hilt.android.EntryPointAccessors

@Composable
fun LockScreen(onUnlock: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    val context = LocalContext.current
    val lockManager: AppLockManager = remember {
        EntryPointAccessors.fromApplication(context, LockEntryPoint::class.java).appLockManager()
    }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val lockoutMs = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) { lockoutMs.value = lockManager.remainingLockoutMs() }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Our own simple logo mark: concentric circles (original design).
            Box(
                modifier = Modifier
                    .size(Sizes.fab + Spacing.lg)
                    .clip(CircleShape)
                    .background(tokens.primary),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(Sizes.fab)
                        .clip(CircleShape)
                        .background(tokens.accent),
                )
            }
            GlassCard(Modifier.padding(top = Spacing.xl)) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Text(
                        text = "Enter passcode",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.textPrimary,
                    )
                    PinDots(filledCount = pin.length.coerceAtMost(6))
                    if (lockoutMs.value > 0) {
                        Text(
                            text = "Too many attempts. Try again in ${lockoutMs.value / 60000} min.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.alert,
                        )
                    } else if (error != null) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.alert,
                        )
                    }
                }
            }
            Keypad(
                modifier = Modifier.padding(top = Spacing.lg),
                onDigit = { d ->
                    if (pin.length < 12) {
                        pin += d.toString()
                        if (pin.length >= 6) {
                            // Panic PIN: wipe immediately, then show a normal-looking
                            // "wrong PIN" so an observer cannot tell a wipe happened.
                            if (lockManager.isPanicPin(pin)) {
                                wiper.wipeAll()
                                error = "Wrong passcode"
                                pin = ""
                                return@Keypad
                            }
                            if (lockManager.unlock(pin)) {
                                onUnlock()
                            } else {
                                if (lockManager.onFailedAttemptAutoWipe()) wiper.wipeAll()
                                error = "Wrong passcode"
                                lockoutMs.value = lockManager.remainingLockoutMs()
                                pin = ""
                            }
                        }
                    }
                },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    MeChatTheme { LockScreen(onUnlock = {}) }
}
