package com.aiphoneguardian.app.ui.screens.aichat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.domain.model.ChatMessage
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.AIChatViewModel

@Composable
fun AIChatScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val canSendMessage by viewModel.canSendMessage.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val remainingMessages by viewModel.remainingMessages.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ChatHeader(
                isPremium = isPremium,
                remainingMessages = remainingMessages,
                onPremiumClick = onNavigateToPremium
            )

            // Messages
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    EmptyChatState()
                } else {
                    ChatMessagesList(
                        messages = messages,
                        isLoading = isLoading
                    )
                }
            }

            // Input
            if (!canSendMessage && !isPremium) {
                ChatLimitBanner(
                    onWatchAd = { viewModel.watchAdForMessage() },
                    onUpgrade = onNavigateToPremium
                )
            }

            ChatInputBar(
                onSendMessage = { viewModel.sendMessage(it) },
                isLoading = isLoading,
                enabled = canSendMessage || isPremium
            )
        }
    }
}

@Composable
fun ChatHeader(
    isPremium: Boolean,
    remainingMessages: Int,
    onPremiumClick: () -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier.padding(8.dp),
        glowColor = NeonCyan
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AI Security Assistant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!isPremium) {
                        Text(
                            text = "$remainingMessages messages left today",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonPurple, ElectricBlue)
                            )
                        )
                        .clickable(onClick = onPremiumClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "UPGRADE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SafeGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "UNLIMITED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = NeonCyan.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI Security Assistant",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ask me anything about your device security, threat analysis, or cybersecurity advice.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Quick prompts
        val prompts = listOf(
            "Is my device secure?",
            "How to detect malware?",
            "What is phishing?",
            "Tips for safe browsing"
        )

        prompts.forEach { prompt ->
            OutlinedButton(
                onClick = { /* Send prompt */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.3f))
            ) {
                Text(prompt, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ChatMessagesList(
    messages: List<ChatMessage>,
    isLoading: Boolean
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            ChatMessageItem(message = message)
        }

        if (isLoading) {
            item {
                AIThinkingIndicator()
            }
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.isFromUser
    val backgroundColor = if (isUser) {
        Brush.horizontalGradient(
            colors = listOf(NeonCyan.copy(alpha = 0.3f), ElectricBlue.copy(alpha = 0.2f))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(GlassWhite.copy(alpha = 0.1f), GlassWhite.copy(alpha = 0.05f))
        )
    }

    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(brush = backgroundColor)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isUser) TextPrimary else TextSecondary,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AIThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotStates = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, delayMillis = index * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$index"
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(0.5f),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GlassWhite.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                dotStates.forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonCyan.copy(alpha = alpha.value))
                    )
                }
            }
        }
    }
}

@Composable
fun ChatLimitBanner(
    onWatchAd: () -> Unit,
    onUpgrade: () -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier.padding(8.dp),
        glowColor = WarningYellow
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Daily chat limit reached",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WarningYellow
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onWatchAd,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Watch Ad", fontSize = 12.sp)
                }
                Button(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upgrade", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    var text by remember { mutableStateOf("") }

    GlassmorphismCard(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about security...", color = TextMuted) },
                singleLine = true,
                enabled = enabled && !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = NeonCyan.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank() && !isLoading && enabled,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (text.isNotBlank() && enabled)
                            Brush.horizontalGradient(colors = listOf(NeonCyan, ElectricBlue))
                        else
                            Brush.horizontalGradient(colors = listOf(TextMuted.copy(alpha = 0.3f), TextMuted.copy(alpha = 0.3f)))
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
