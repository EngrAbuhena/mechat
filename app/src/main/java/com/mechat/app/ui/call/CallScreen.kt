package com.mechat.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraSwitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.PillChip
import com.mechat.app.ui.components.ChipVariant
import com.mechat.app.ui.components.RoundIconButton
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme
import com.mechat.app.ui.theme.Sizes
import com.mechat.app.ui.theme.Spacing

enum class CallState { RINGING_IN, RINGING_OUT, ACTIVE, ENDED }

/**
 * 1:1 voice/video call UI. Signalling rides the encrypted channel; media is
 * SRTP/DTLS via WebRTC. The safety number (fingerprint of both keys) is shown.
 * CAMERA/RECORD_AUDIO are requested only when the call starts.
 */
@Composable
fun CallScreen(
    state: CallState = CallState.RINGING_IN,
    contactName: String = "Alice",
    safetyNumber: String = "00000 00000 00000 00000 00000 00000 00000 00000 00000 00000 00000 00000",
    isVideo: Boolean = false,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onEnd: () -> Unit = {},
) {
    val tokens = LocalMeChatTokens.current
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }

    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (state) {
                        CallState.RINGING_IN -> "Incoming call…"
                        CallState.RINGING_OUT -> "Calling…"
                        CallState.ACTIVE -> "Connected"
                        CallState.ENDED -> "Call ended"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.textPrimary,
                )
                Text(contactName, style = MaterialTheme.typography.displayLarge, color = tokens.textPrimary)
                // Safety number / fingerprint of the call — verify out of band.
                PillChip(text = safetyNumber, variant = ChipVariant.NEUTRAL, modifier = Modifier.padding(top = Spacing.md))
            }
            if (state == CallState.RINGING_IN) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xl)) {
                    RoundIconButton(Icons.Default.Call, "Accept", onAccept, containerColor = tokens.success, iconColor = tokens.onPrimary)
                    RoundIconButton(Icons.Default.CallEnd, "Decline", onDecline, containerColor = tokens.alert, iconColor = androidx.compose.ui.graphics.Color.White)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundIconButton(
                        icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (muted) "Unmute" else "Mute",
                        onClick = { muted = !muted },
                        containerColor = tokens.glassSurface,
                    )
                    RoundIconButton(Icons.Default.CallEnd, "End call", onEnd, containerColor = tokens.alert, iconColor = androidx.compose.ui.graphics.Color.White)
                    RoundIconButton(
                        icon = Icons.Default.VolumeUp,
                        contentDescription = if (speaker) "Speaker off" else "Speaker on",
                        onClick = { speaker = !speaker },
                        containerColor = tokens.glassSurface,
                    )
                    if (isVideo) {
                        RoundIconButton(Icons.Default.CameraSwitch, "Switch camera", {}, containerColor = tokens.glassSurface)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CallScreenPreview() {
    MeChatTheme { CallScreen(state = CallState.ACTIVE) }
}
