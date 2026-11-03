package com.mechat.app.ui.navigation

import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mechat.app.ui.addcontact.AddContactScreen
import com.mechat.app.ui.call.CallScreen
import com.mechat.app.ui.chat.ChatScreen
import com.mechat.app.ui.chatlist.ChatListScreen
import com.mechat.app.ui.lock.LockScreen
import com.mechat.app.ui.onboarding.OnboardingScreen
import com.mechat.app.ui.settings.SettingsScreen

private const val TransitionMs = 200

/** True when the system asks for reduced motion (animator duration scale = 0). */
private fun reduceMotion(context: android.content.Context): Boolean =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f

@Composable
fun MeChatNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val reduced = reduceMotion(context)
    val fadeIn = fadeIn(if (reduced) tween(1) else tween(TransitionMs))
    val fadeOut = fadeOut(if (reduced) tween(1) else tween(TransitionMs))
    val slideIn = slideInHorizontally { fullWidth -> if (reduced) 0 else fullWidth / 8 }
    val slideOut = slideOutHorizontally { fullWidth -> if (reduced) 0 else -fullWidth / 8 }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Onboarding,
    ) {
        composable(NavRoutes.Onboarding, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            OnboardingScreen(onDone = { navController.navigate(NavRoutes.SetPin) })
        }
        composable(NavRoutes.SetPin, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            com.mechat.app.ui.onboarding.SetPinScreen(onDone = {
                navController.navigate(NavRoutes.Lock) {
                    popUpTo(NavRoutes.Onboarding) { inclusive = true }
                }
            })
        }
        composable(NavRoutes.Lock, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            LockScreen(onUnlock = {
                navController.navigate(NavRoutes.ChatList) {
                    popUpTo(NavRoutes.Lock) { inclusive = true }
                }
            })
        }
        composable(NavRoutes.ChatList, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            ChatListScreen(
                onConversationClick = { navController.navigate(NavRoutes.chat(it)) },
                onAddClick = { navController.navigate(NavRoutes.AddContact) },
                onContactsClick = { navController.navigate(NavRoutes.Contacts) },
                onSettingsClick = { navController.navigate(NavRoutes.Settings) },
            )
        }
        composable(
            NavRoutes.Chat,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = { slideIn + fadeIn },
            exitTransition = { slideOut + fadeOut },
        ) { backStackEntry ->
            ChatScreen(
                conversationId = backStackEntry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
                onCallClick = { navController.navigate(NavRoutes.Call) },
            )
        }
        composable(NavRoutes.AddContact, enterTransition = { slideIn + fadeIn }, exitTransition = { slideOut + fadeOut }) {
            AddContactScreen(
                onClose = { navController.popBackStack() },
                onScan = { navController.navigate(NavRoutes.ScanQr) },
            )
        }
        composable(NavRoutes.ScanQr, enterTransition = { slideIn + fadeIn }, exitTransition = { slideOut + fadeOut }) {
            com.mechat.app.ui.scan.ScanQrScreen(
                onScanned = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.Contacts, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            com.mechat.app.ui.chatlist.ContactsScreen()
        }
        composable(NavRoutes.Settings, enterTransition = { slideIn + fadeIn }, exitTransition = { slideOut + fadeOut }) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.Call, enterTransition = { fadeIn }, exitTransition = { fadeOut }) {
            CallScreen(onEnd = { navController.popBackStack() })
        }
    }
}
