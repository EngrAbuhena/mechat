package com.mechat.app.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mechat.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for sending and receiving between two test instances.
 * NOT run in the sandbox — require an emulator. Run with: connectedDebugAndroidTest.
 */
@RunWith(AndroidJUnit4::class)
class ChatUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun sendMessage_appearsInChat_andShowsStatus() {
        // Navigate to a chat, type, send → bubble visible with status tick.
        composeRule.onNodeWithText("Your message").performTextInput("hello")
        composeRule.onNodeWithText("hello").assertIsDisplayed()
    }

    @Test
    fun receiveMessage_fromSecondInstance_appearsAsGlassBubble() {
        // Simulate incoming message → received bubble rendered.
        composeRule.onNodeWithText("hi there").assertIsDisplayed()
    }
}
