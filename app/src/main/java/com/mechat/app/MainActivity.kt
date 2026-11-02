package com.mechat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mechat.app.ui.navigation.MeChatNavHost
import com.mechat.app.ui.theme.MeChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // While locked, the Recents thumbnail must be blank.
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
        )
        enableEdgeToEdge()
        setContent {
            MeChatTheme {
                val navController = rememberNavController()
                androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
                    MeChatNavHost(navController)
                }
            }
        }
    }
}
