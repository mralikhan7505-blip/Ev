package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessageEntity
import com.example.data.model.AiMode
import com.example.data.model.SearchSource
import com.example.ui.theme.EvAmber
import com.example.ui.theme.EvCyan
import com.example.ui.theme.EvGoogleBlue
import com.example.ui.theme.EvMint
import com.example.ui.theme.EvTextPrimary
import com.example.ui.theme.EvTextSecondary
import com.example.ui.theme.EvViolet
import org.json.JSONArray

@Composable
fun EvOrbIndicator(
    isThinking: Boolean,
    mode: AiMode,
    modifier: Modifier = Modifier,
    sizeDp: Int = 40
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val currentScale = if (isThinking) scale else 1.0f

    val colors = when (mode) {
        AiMode.HIGH_THINKING -> listOf(EvAmber, EvViolet, Color(0xFFFF6D00))
        AiMode.GOOGLE_SEARCH -> listOf(EvGoogleBlue, EvCyan, Color(0xFF34A853))
        AiMode.STANDARD -> listOf(EvCyan, EvViolet, EvMint)
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(currentScale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = colors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (mode) {
                AiMode.HIGH_THINKING -> Icons.Default.Psychology
                AiMode.GOOGLE_SEARCH -> Icons.Default.Search
                AiMode.STANDARD -> Icons.Default.AutoAwesome
            },
            contentDescription = "Ev Avatar",
            tint = Color.White,
            modifier = Modifier.size((sizeDp * 0.55).dp)
        )
    }
}

@Composable
fun AiModeSelector(
    selectedMode: AiMode,
    onSelectMode: (AiMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AiMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val accentColor = when (mode) {
                AiMode.HIGH_THINKING -> EvAmber
                AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                AiMode.STANDARD -> EvCyan
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectMode(mode) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (mode) {
                                AiMode.HIGH_THINKING -> Icons.Default.Psychology
                                AiMode.GOOGLE_SEARCH -> Icons.Default.Search
                                AiMode.STANDARD -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) accentColor else EvTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else EvTextSecondary
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.22f),
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = accentColor
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).testTag("mode_chip_${mode.name.lowercase()}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    onTeachMemory: (content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var isThinkingExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val sources = remember(message.searchSourcesJson) {
        val list = mutableListOf<SearchSource>()
        message.searchSourcesJson?.let { jsonStr ->
            runCatching {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SearchSource(
                            title = obj.getString("title"),
                            url = obj.getString("url")
                        )
                    )
                }
            }
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                EvOrbIndicator(
                    isThinking = false,
                    mode = message.mode,
                    sizeDp = 22
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ev • ${message.mode.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (message.mode) {
                        AiMode.HIGH_THINKING -> EvAmber
                        AiMode.GOOGLE_SEARCH -> EvGoogleBlue
                        AiMode.STANDARD -> EvCyan
                    }
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                Color(0xFF1E3A8A)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isUser) Color(0xFF3B82F6) else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // High Thinking Process Card (if present)
                if (!message.thinkingProcess.isNullOrBlank()) {
                    ThinkingProcessCard(
                        thinkingText = message.thinkingProcess,
                        isExpanded = isThinkingExpanded,
                        onToggle = { isThinkingExpanded = !isThinkingExpanded }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Main Message Text
                Text(
                    text = message.text,
                    color = EvTextPrimary,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp
                )

                // Google Search Sources (if present)
                if (sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sources & Citations:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvGoogleBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sources.forEach { source ->
                            AssistChip(
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                        context.startActivity(intent)
                                    }
                                },
                                label = {
                                    Text(
                                        text = source.title.take(28) + if (source.title.length > 28) "…" else "",
                                        fontSize = 11.sp,
                                        color = EvTextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = EvGoogleBlue
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = EvTextSecondary
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = EvGoogleBlue.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Bottom Action bar for assistant messages
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                isCopied = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = if (isCopied) EvMint else EvTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { onTeachMemory(message.text) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Save to Ev Memory",
                                tint = EvCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingProcessCard(
    thinkingText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvAmber.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EvAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = EvAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ev's High Thinking Reasoning",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvAmber
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = EvAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = thinkingText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = EvTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
