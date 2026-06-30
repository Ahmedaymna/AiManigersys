package com.aiphoneguardian.app.ui.screens.calls

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aiphoneguardian.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class CallEntry(
    val id: Long,
    val number: String,
    val name: String?,
    val date: Long,
    val duration: Long,
    val type: Int
)

@Composable
fun CallsScreen() {
    val context = LocalContext.current
    var calls by remember { mutableStateOf<List<CallEntry>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALL_LOG
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val list = mutableListOf<CallEntry>()
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                ),
                null, null, "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = it.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                while (it.moveToNext() && list.size < 150) {
                    list.add(CallEntry(
                        id = it.getLong(idIdx),
                        number = it.getString(numIdx) ?: "مجهول",
                        name = it.getString(nameIdx),
                        date = it.getLong(dateIdx),
                        duration = it.getLong(durIdx),
                        type = it.getInt(typeIdx)
                    ))
                }
            }
            calls = list
        }
    }

    val filtered = calls.filter { call ->
        val matchQ = searchQuery.isEmpty() ||
            call.number.contains(searchQuery) ||
            call.name?.contains(searchQuery, true) == true
        val matchTab = when (selectedTab) {
            1 -> call.type == CallLog.Calls.INCOMING_TYPE
            2 -> call.type == CallLog.Calls.OUTGOING_TYPE
            3 -> call.type == CallLog.Calls.MISSED_TYPE
            else -> true
        }
        matchQ && matchTab
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SpaceDark, Color(0xFF0A0A1A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceCard.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NeonGreen.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("المكالمات", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))

                // Quick stats
                val missed = calls.count { it.type == CallLog.Calls.MISSED_TYPE }
                if (missed > 0) {
                    Surface(color = AlertRed.copy(0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text("  $missed فائتة  ", color = AlertRed, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("بحث بالرقم أو الاسم...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = NeonGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = SpaceCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceCard.copy(alpha = 0.8f),
                contentColor = NeonGreen,
                edgePadding = 8.dp
            ) {
                listOf("الكل", "واردة", "صادرة", "فائتة").forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            if (!hasPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("صلاحية سجل المكالمات غير ممنوحة", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else if (calls.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { call ->
                        CallCard(call)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallCard(call: CallEntry) {
    val (icon, color) = when (call.type) {
        CallLog.Calls.INCOMING_TYPE -> Icons.Default.CallReceived to NeonGreen
        CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade to NeonCyan
        CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed to AlertRed
        else -> Icons.Default.Phone to TextMuted
    }
    val formatter = remember { SimpleDateFormat("dd/MM hh:mm a", Locale.getDefault()) }
    val durationStr = if (call.duration > 0) {
        val m = call.duration / 60; val s = call.duration % 60
        if (m > 0) "${m}د ${s}ث" else "${s}ث"
    } else ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SpaceCard.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    call.name ?: call.number,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (call.name != null) {
                    Text(call.number, color = TextMuted, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatter.format(Date(call.date)), color = TextMuted, fontSize = 10.sp)
                if (durationStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(durationStr, color = color, fontSize = 11.sp)
                }
            }
        }
    }
}
