package com.aiphoneguardian.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object FileGuardian : Screen("file_guardian")
    object NetworkMonitor : Screen("network_monitor")
    object AIChat : Screen("ai_chat")
    object Settings : Screen("settings")
    object Premium : Screen("premium")
    object Permissions : Screen("permissions")
    object Messages : Screen("messages")
    object Calls : Screen("calls")
    object CallLog : Screen("call_log")

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Scanner,
            Messages,
            Calls,
            AIChat,
            Settings
        )
    }
}
