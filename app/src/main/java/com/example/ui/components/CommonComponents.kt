package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.PronunciationResult
import com.example.ui.theme.*

@Composable
fun CefrLevelBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (level.uppercase()) {
        "A1" -> Pair(ColorA1.copy(alpha = 0.15f), ColorA1)
        "A2" -> Pair(ColorA2.copy(alpha = 0.15f), ColorA2)
        "B1" -> Pair(ColorB1.copy(alpha = 0.15f), ColorB1)
        "B2" -> Pair(ColorB2.copy(alpha = 0.15f), ColorB2)
        "C1" -> Pair(ColorC1.copy(alpha = 0.15f), ColorC1)
        "C2" -> Pair(ColorC2.copy(alpha = 0.15f), ColorC2)
        else -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = level.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun AccentSelector(
    selectedAccent: String,
    onAccentChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val usSelected = selectedAccent == "US"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (usSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onAccentChanged("US") }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🇺🇸 US (GA)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (usSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val ukSelected = selectedAccent == "UK"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (ukSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onAccentChanged("UK") }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🇬🇧 UK (RP)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ukSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AudioPlayIconButton(
    textToSpeak: String,
    onPlay: (String, String?) -> Unit,
    accent: String? = null,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    FilledTonalIconButton(
        onClick = { onPlay(textToSpeak, accent) },
        modifier = modifier.size(size.dp)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "استماع للنطق",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PronunciationPracticeCard(
    targetText: String,
    accent: String,
    isListening: Boolean,
    result: PronunciationResult?,
    errorMessage: String?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onPlayReference: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تدريب على النطق وتقييم الصوت",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"$targetText\"",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Listen to reference
                OutlinedButton(
                    onClick = onPlayReference,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "استمع للنموذج ($accent)", fontSize = 12.sp)
                }

                // Record microphone button
                Button(
                    onClick = {
                        if (isListening) onStopListening() else onStartListening()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "تسجيل النطق",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isListening) "جارِ الاستماع..." else "انطق الآن",
                        fontSize = 12.sp
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(visible = result != null) {
                result?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (it.scorePercentage >= 75) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val scoreColor = if (it.scorePercentage >= 75) Color(0xFF15803D) else Color(0xFFB45309)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "دقة النطق: ${it.scorePercentage}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = scoreColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ما نطقته: \"${it.spokenText}\"",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it.feedbackAr,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = scoreColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
