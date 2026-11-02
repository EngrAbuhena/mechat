package com.mechat.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.GlassCard
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val Pages = listOf(
    OnboardingPage(
        "No account",
        "MeChat has no phone number, email or username. Your identity is a pair of keys generated on this device — nothing to sign up for, nothing to recover.",
    ),
    OnboardingPage(
        "No recovery",
        "If you lose this phone, your identity is gone with it. There is no password reset and no backup server — that is the price of having no account.",
    ),
    OnboardingPage(
        "Your PIN",
        "A 6-digit PIN locks the app on this device. It protects the keys stored locally; it is never sent anywhere.",
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val tokens = LocalMeChatTokens.current
    val pagerState = rememberPagerState(pageCount = { Pages.size })
    val scope = rememberCoroutineScope()

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Text(
                            text = Pages[page].title,
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.textPrimary,
                        )
                        Text(
                            text = Pages[page].body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = tokens.textSecondary,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDone) {
                    Text("Skip", color = tokens.textSecondary)
                }
                Button(
                    onClick = {
                        if (pagerState.currentPage < Pages.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else onDone()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                ) {
                    Text(
                        if (pagerState.currentPage < Pages.lastIndex) "Next" else "Continue",
                        color = tokens.onPrimary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MeChatTheme { OnboardingScreen(onDone = {}) }
}
