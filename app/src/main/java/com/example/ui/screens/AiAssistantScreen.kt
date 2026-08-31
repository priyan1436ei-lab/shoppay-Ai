package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayUiState
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    state: ShopPayUiState,
    onSendMessage: (String) -> Unit
) {
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val currency = state.profile.currency
    val forecast = state.forecast

    val suggestedPrompts = listOf(
        "How much did I earn this week?",
        "What was my best day?",
        "Which payment method do customers use most?",
        "Compare this month with target",
        "What is my expected revenue next week?",
        "Any unusual transactions?"
    )

    LaunchedEffect(state.chatMessages.size, state.isAiThinking) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = ElegantLilac,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ShopPay AI Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Instant Financial Intelligence",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .testTag("ai_assistant_screen")
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Predictive Income Forecasting Card at top of AI Assistant
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(22.dp))
                            .testTag("ai_forecast_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = ElegantLilac,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "AI INCOME FORECAST",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = ElegantLilac
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ElegantPurpleDark
                                ) {
                                    Text(
                                        text = "Moving Average",
                                        color = ElegantLilac,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Expected Tomorrow", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            "$currency${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMin)} - $currency${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMax)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ElegantGreen
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Next 7 Days", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            "$currency${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMin)} - $currency${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMax)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "💡 ${forecast.confidenceNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Chat message bubbles
                items(state.chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        onSuggestedActionClick = { action ->
                            onSendMessage(action)
                        }
                    )
                }

                // Thinking state indicator
                if (state.isAiThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ElegantLilac
                            )
                            Text(
                                text = "Analyzing store transaction data...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Suggested prompt chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedPrompts.forEach { prompt ->
                    SuggestionChip(
                        onClick = {
                            onSendMessage(prompt)
                        },
                        label = {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = DarkSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Input Bar
            Surface(
                color = DarkSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Ask about income, top days, payment mix...", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_ai_query")
                    )

                    FilledIconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                val query = inputQuery.trim()
                                inputQuery = ""
                                onSendMessage(query)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ElegantLilac,
                            contentColor = ElegantPurpleDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("btn_send_ai_query")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onSuggestedActionClick: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ElegantPurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = ElegantLilac,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) ElegantPurpleDark else DarkSurface,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0x14FFFFFF)),
                tonalElevation = if (isUser) 0.dp else 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else TextPrimary,
                        lineHeight = 22.sp
                    )

                    if (!isUser && message.suggestedActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            message.suggestedActions.forEach { action ->
                                OutlinedButton(
                                    onClick = { onSuggestedActionClick(action) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantLilac),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(action, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
