package com.aiphoneguardian.app.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiphoneguardian.app.ui.theme.*

data class PermissionGroup(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color,
    val permissions: List<String>
)

@Composable
fun PermissionsScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current

    val allPermissions = buildList {
        addAll(listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionGroups = listOf(
        PermissionGroup(
            Icons.Default.LocationOn, "الموقع", "لتتبع التهديدات المحلية وحماية شبكتك",
            NeonCyan,
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ),
        PermissionGroup(
            Icons.Default.Phone, "الهاتف والمكالمات", "لمراقبة المكالمات المشبوهة وسجل الاتصالات",
            NeonPurple,
            listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG)
        ),
        PermissionGroup(
            Icons.Default.Message, "الرسائل", "لفحص الرسائل من التصيد الاحتيالي والبرامج الضارة",
            NeonGreen,
            listOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        ),
        PermissionGroup(
            Icons.Default.Contacts, "جهات الاتصال", "لحماية بياناتك وكشف الوصول غير المصرح",
            Color(0xFFFF9800),
            listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        ),
        PermissionGroup(
            Icons.Default.CameraAlt, "الكاميرا والميكروفون", "لتمكين AI Vision والمراقبة الأمنية",
            Color(0xFFE91E63),
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        ),
        PermissionGroup(
            Icons.Default.Storage, "التخزين", "لفحص الملفات وكشف البرامج الضارة المخفية",
            Color(0xFF9C27B0),
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ),
    )

    var granted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        granted = results.values.any { it }
        if (granted) onAllGranted()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(SpaceDark, Color(0xFF0A0A1A), SpaceCard))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shield Icon
            Box(
                modifier = Modifier
                    .size((80 * pulse).dp)
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "🛡️ تفعيل الحماية الكاملة",
                color = NeonCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "نحتاج هذه الصلاحيات لتوفير حماية شاملة لجهازك\nسيتم طلبها دفعة واحدة فقط",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            permissionGroups.forEach { group ->
                PermissionGroupCard(group)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grant Button
            Button(
                onClick = { launcher.launch(allPermissions.toTypedArray()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = SpaceDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "منح جميع الصلاحيات",
                            color = SpaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAllGranted) {
                Text("تخطي الآن", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PermissionGroupCard(group: PermissionGroup) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SpaceCard.copy(alpha = 0.8f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(group.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(group.icon, contentDescription = null, tint = group.color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(group.description, color = TextMuted, fontSize = 11.sp)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = group.color.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}
