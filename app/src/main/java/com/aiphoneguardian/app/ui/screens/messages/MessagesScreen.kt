package com.aiphoneguardian.app.ui.screens.messages

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aiphoneguardian.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int // 1=inbox, 2=sent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val smsList = mutableListOf<SmsMessage>()
            val uri = Uri.parse("content://sms/")
            val cursor: Cursor? = context.contentResolver.query(
                uri, arrayOf("_id", "address", "body", "date", "type"),
                null, null, "date DESC"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex("_id")
                val addrIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")
                val typeIdx = it.getColumnIndex("type")
                while (it.moveToNext() && smsList.size < 100) {
                    smsList.add(SmsMessage(
                        id = it.getLong(idIdx),
                        address = it.getString(addrIdx) ?: "مجهول",
                        body = it.getString(bodyIdx) ?: "",
                        date = it.getLong(dateIdx),
                        type = it.getInt(typeIdx)
                    ))
                }
            }
            messages = smsList
        }
    }

    val filtered = messages.filter { msg ->
        val matchQuery = searchQuery.isEmpty() ||
            msg.address.contains(searchQuery) ||
            msg.body.contains(searchQuery, ignoreCase = true)
        val matchTab = when (selectedTab) {
            1 -> msg.type == 1 // inbox
            2 -> msg.type == 2 // sent
            else -> true
        }
        matchQuery && matchTab
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
                        .background(NeonCyan.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Message, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("الرسائل", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${filtered.size}", color = TextMuted, fontSize = 12.sp)
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("بحث في الرسائل...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = NeonCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SpaceCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = SpaceCard.copy(alpha = 0.8f),
                contentColor = NeonCyan
            ) {
                listOf("الكل", "الوارد", "المُرسل").forEachIndexed { idx, title ->
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
                        Text("صلاحية قراءة الرسائل غير ممنوحة", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { msg ->
                        SmsCard(msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsCard(msg: SmsMessage) {
    val isInbox = msg.type == 1
    val accentColor = if (isInbox) NeonCyan else NeonPurple
    val formatter = remember { SimpleDateFormat("dd/MM hh:mm a", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SpaceCard.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isInbox) Icons.Default.CallReceived else Icons.Default.CallMade,
                    null, tint = accentColor, modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(msg.address, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(formatter.format(Date(msg.date)), color = TextMuted, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(msg.body, color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
