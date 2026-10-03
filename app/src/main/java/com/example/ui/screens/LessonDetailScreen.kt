package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.ui.components.AudioPlayIconButton
import com.example.ui.components.CefrLevelBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lesson: Lesson,
    preferredAccent: String,
    onClose: () -> Unit,
    onPlayAudio: (String, String?) -> Unit,
    onComplete: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val scrollState = rememberScrollState()

    // Parse examples: "English|||Arabic###English2|||Arabic2"
    val examples = remember(lesson.examplesJson) {
        lesson.examplesJson.split("###").mapNotNull { item ->
            val parts = item.split("|||")
            if (parts.size >= 2) Pair(parts[0], parts[1]) else null
        }
    }

    // Parse quiz: "Question?|||OptA|||OptB|||OptC|||CorrectIndex|||Explanation"
    val quizzes = remember(lesson.quizQuestionsJson) {
        lesson.quizQuestionsJson.split("###").mapNotNull { item ->
            val parts = item.split("|||")
            if (parts.size >= 6) {
                object {
                    val question = parts[0]
                    val options = listOf(parts[1], parts[2], parts[3])
                    val correctIdx = parts[4].toIntOrNull() ?: 0
                    val explanation = parts[5]
                }
            } else null
        }
    }

    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lesson.title,
                        maxLines = 1,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    CefrLevelBadge(level = lesson.cefrLevel, modifier = Modifier.padding(end = 12.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = lesson.arabicTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "القاعدة: ${lesson.grammarFocus}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Explanation Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الشرح والتفصيل النحوي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = lesson.explanationAr,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "English Reference:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.explanationEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Examples Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أمثلة تطبيقية مع النطق",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (preferredAccent == "UK") "🇬🇧 UK" else "🇺🇸 US",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    examples.forEach { (en, ar) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = en,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ar,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AudioPlayIconButton(
                                textToSpeak = en,
                                onPlay = { txt, _ -> onPlayAudio(txt, preferredAccent) },
                                size = 36
                            )
                        }
                    }
                }
            }

            // Interactive Mini Quiz
            if (quizzes.isNotEmpty()) {
                val currentQuiz = quizzes.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تمرين الفهم الفوري",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentQuiz.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        currentQuiz.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptionIndex == index
                            val isCorrect = index == currentQuiz.correctIdx
                            val btnColor = when {
                                !quizSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                quizSubmitted && isCorrect -> Color(0xFFDCFCE7)
                                quizSubmitted && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !quizSubmitted) {
                                        selectedOptionIndex = index
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = btnColor),
                                border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { if (!quizSubmitted) selectedOptionIndex = index }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = option, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!quizSubmitted) {
                            Button(
                                onClick = {
                                    if (selectedOptionIndex != null) {
                                        quizSubmitted = true
                                    }
                                },
                                enabled = selectedOptionIndex != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تحقق من الإجابة")
                            }
                        } else {
                            val isRight = selectedOptionIndex == currentQuiz.correctIdx
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRight) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (isRight) "إجابة صحيحة! (+50 XP) 🎉" else "إجابة غير صحيحة، ركز في القاعدة:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isRight) Color(0xFF15803D) else Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentQuiz.explanation,
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Complete Lesson Button
            Button(
                onClick = {
                    onComplete(lesson)
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("complete_lesson_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (lesson.isCompleted) "تم إكمال الدرس (مراجعة)" else "إتمام الدرس واحتساب النقاط")
            }
        }
    }
}
