package com.aiphoneguardian.app.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.AlertDialog
import com.aiphoneguardian.app.domain.model.ScanSchedule
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToPremium: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = true)
    val language by viewModel.language.collectAsState(initial = "en")
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState(initial = true)
    val scanSchedule by viewModel.scanSchedule.collectAsState(initial = ScanSchedule.DAILY)
    val isPremium by viewModel.isPremium.collectAsState(initial = false)
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Card (if not premium)
            if (!isPremium) {
                GlassmorphismCard(
                    glowColor = NeonPurple,
                    onClick = onNavigateToPremium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(NeonPurple, ElectricBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upgrade to Premium",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Unlock full AI protection",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Use dark color scheme",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Get threat alerts and updates",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scan Schedule
            SettingsSection(title = "Scan Schedule") {
                ScanScheduleSelector(
                    currentSchedule = scanSchedule,
                    onScheduleChange = { viewModel.setScanSchedule(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language
            SettingsSection(title = "Language") {
                LanguageSelector(
                    currentLanguage = language,
                    onLanguageChange = { viewModel.setLanguage(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account
            SettingsSection(title = "Account") {
                SettingsClickableItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    iconColor = CriticalRed,
                    onClick = { viewModel.showLogoutDialog() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About
            SettingsSection(title = "About") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = "1.0.0"
                )
                SettingsClickableItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = { /* Open privacy policy */ }
                )
                SettingsClickableItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Read terms of service",
                    onClick = { /* Open terms */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutDialog() },
            title = { Text("Logout", color = TextPrimary) },
            text = { Text("Are you sure you want to logout?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideLogoutDialog()
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideLogoutDialog() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SpaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        GlassmorphismCard {
            content()
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = NeonCyan
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun ScanScheduleSelector(
    currentSchedule: ScanSchedule,
    onScheduleChange: (ScanSchedule) -> Unit
) {
    Column {
        ScanScheduleOption(
            label = "Daily",
            selected = currentSchedule == ScanSchedule.DAILY,
            onSelect = { onScheduleChange(ScanSchedule.DAILY) }
        )
        Divider(color = GlassBorder.copy(alpha = 0.2f), thickness = 1.dp)
        ScanScheduleOption(
            label = "Weekly",
            selected = currentSchedule == ScanSchedule.WEEKLY,
            onSelect = { onScheduleChange(ScanSchedule.WEEKLY) }
        )
        Divider(color = GlassBorder.copy(alpha = 0.2f), thickness = 1.dp)
        ScanScheduleOption(
            label = "Manual Only",
            selected = currentSchedule == ScanSchedule.MANUAL,
            onSelect = { onScheduleChange(ScanSchedule.MANUAL) }
        )
    }
}

@Composable
fun ScanScheduleOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = if (selected) NeonCyan else TextSecondary
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LanguageOption(
            label = "English",
            code = "en",
            selected = currentLanguage == "en",
            onSelect = { onLanguageChange("en") },
            modifier = Modifier.weight(1f)
        )
        LanguageOption(
            label = "Arabic",
            code = "ar",
            selected = currentLanguage == "ar",
            onSelect = { onLanguageChange("ar") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguageOption(
    label: String,
    code: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) NeonCyan.copy(alpha = 0.2f)
                else GlassWhite.copy(alpha = 0.05f)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NeonCyan.copy(alpha = 0.6f) else GlassBorder.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) NeonCyan else TextSecondary
        )
    }
}
