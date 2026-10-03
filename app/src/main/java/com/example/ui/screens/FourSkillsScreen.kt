package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SkillExercise
import com.example.ui.components.AccentSelector
import com.example.ui.components.AudioPlayIconButton
import com.example.ui.components.CefrLevelBadge
import com.example.ui.viewmodel.EnglishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourSkillsScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val activeSkillType by viewModel.activeSkillType.collectAsState()
    val preferredAccent by viewModel.preferredAccent.collectAsState()

    val listeningExercises by viewModel.listeningExercises.collectAsState()
    val readingExercises by viewModel.readingExercises.collectAsState()
    val speakingExercises by viewModel.speakingExercises.collectAsState()
    val writingExercises by viewModel.writingExercises.collectAsState()

    val roleplayMessages by viewModel.roleplayMessages.collectAsState()
    val isRoleplayLoading by viewModel.isRoleplayLoading.collectAsState()

    val writingEval by viewModel.writingEvaluation.collectAsState()
    val isEvaluatingWriting by viewModel.isEvaluatingWriting.collectAsState()

    var userChatInput by remember { mutableStateOf("") }
    var selectedScenario by remember { mutableStateOf("Tech Job Interview") }

    var userWritingInput by remember { mutableStateOf("He a good engineer and I am agree with his opinion.") }

    var lookupWordDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "قسم المهارات الأربع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "The 4 Core English Skills",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AccentSelector(
                selectedAccent = preferredAccent,
                onAccentChanged = { viewModel.setPreferredAccent(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Skills Segmented Tab Row
        val skills = listOf("LISTENING", "READING", "SPEAKING", "WRITING")
        val skillLabels = mapOf(
            "LISTENING" to "🎧 استماع",
            "READING" to "📖 قراءة",
            "SPEAKING" to "🗣️ تحدث",
            "WRITING" to "✍️ كتابة"
        )

        ScrollableTabRow(
            selectedTabIndex = skills.indexOf(activeSkillType),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            skills.forEach { skill ->
                Tab(
                    selected = activeSkillType == skill,
                    onClick = { viewModel.setActiveSkillType(skill) },
                    text = { Text(skillLabels[skill] ?: skill, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skill Content Container
        when (activeSkillType) {
            "LISTENING" -> {
                // LISTENING MODULE
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(listeningExercises) { exercise ->
                        var showTranscript by remember { mutableStateOf(false) }
                        var answeredQuiz by remember { mutableStateOf(false) }
                        var selectedQuizAnswer by remember { mutableStateOf<Int?>(null) }

                        val quizItems = remember(exercise.questionsJson) {
                            exercise.questionsJson.split("###").mapNotNull { qStr ->
                                val parts = qStr.split("|||")
                                if (parts.size >= 6) {
                                    object {
                                        val q = parts[0]
                                        val options = listOf(parts[1], parts[2], parts[3])
                                        val correctIdx = parts[4].toIntOrNull() ?: 0
                                        val exp = parts[5]
                                    }
                                } else null
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CefrLevelBadge(level = exercise.cefrLevel)
                                    Text(
                                        text = "بودكاست تدريبي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = exercise.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = exercise.arabicTitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Audio Playback bar
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            FilledIconButton(
                                                onClick = { viewModel.playText(exercise.contentEn, preferredAccent) }
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل المقطع")
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(text = "استمع للنص كاملاً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = "باللكنة ${if (preferredAccent == "UK") "البريطانية (RP)" else "الأمريكية (GA)"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        TextButton(onClick = { showTranscript = !showTranscript }) {
                                            Text(if (showTranscript) "إخفاء النص" else "إظهار النص (Transcript)", fontSize = 11.sp)
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = showTranscript) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = exercise.contentEn,
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = exercise.contentArSummary,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Comprehension quiz
                                if (quizItems.isNotEmpty()) {
                                    val q = quizItems.first()
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "سؤال الفهم السمعي: ${q.q}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    q.options.forEachIndexed { idx, opt ->
                                        val isSelected = selectedQuizAnswer == idx
                                        val isCorrect = idx == q.correctIdx
                                        val optBg = when {
                                            !answeredQuiz && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            answeredQuiz && isCorrect -> Color(0xFFDCFCE7)
                                            answeredQuiz && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable(enabled = !answeredQuiz) { selectedQuizAnswer = idx },
                                            shape = RoundedCornerShape(8.dp),
                                            color = optBg
                                        ) {
                                            Text(
                                                text = opt,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }

                                    if (!answeredQuiz) {
                                        Button(
                                            onClick = {
                                                if (selectedQuizAnswer != null) {
                                                    answeredQuiz = true
                                                    viewModel.playText("Checking answer")
                                                }
                                            },
                                            enabled = selectedQuizAnswer != null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("تأكيد إجابة الاستماع")
                                        }
                                    } else {
                                        val correct = selectedQuizAnswer == q.correctIdx
                                        Text(
                                            text = if (correct) "صحيح! استيعاب سمعي ممتاز! 🎉" else "غير صحيح، الشرح: ${q.exp}",
                                            color = if (correct) Color(0xFF15803D) else Color(0xFFB45309),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "READING" -> {
                // READING MODULE WITH INTERACTIVE WORD LOOKUP
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(readingExercises) { exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CefrLevelBadge(level = exercise.cefrLevel)
                                    Text(
                                        text = "مقال تفاعلي مع ترجمة الكلمات",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = exercise.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = exercise.arabicTitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "💡 انقر على أي كلمة أدناه لسماع نطقها:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Render words as clickable chips / flow row
                                        val words = exercise.contentEn.split(" ")
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = exercise.contentEn,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Quick interactive word buttons
                                        Text(
                                            text = "مفردات هامة في المقال:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val keyWords = listOf("Cognitive", "Relativity", "Bilingual", "Flexibility", "Neural")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            keyWords.forEach { kw ->
                                                AssistChip(
                                                    onClick = {
                                                        lookupWordDialog = kw
                                                        viewModel.playText(kw, preferredAccent)
                                                    },
                                                    label = { Text(kw, fontSize = 11.sp) },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ملخص الفكرة: ${exercise.contentArSummary}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            "SPEAKING" -> {
                // SPEAKING MODULE: AI CONVERSATIONAL PARTNER
                Column(modifier = Modifier.fillMaxSize()) {
                    // Scenario selection chips
                    val scenarios = listOf("Tech Job Interview", "Coffee Shop / Cafe", "Airport Check-in")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scenarios.forEach { sc ->
                            val isSel = selectedScenario == sc
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedScenario = sc
                                    viewModel.clearRoleplayChat(sc)
                                },
                                label = { Text(sc, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Chat messages list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(roleplayMessages) { msg ->
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.text,
                                            fontSize = 13.sp,
                                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!isUser && msg.arabicTranslation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.arabicTranslation,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        if (!isUser) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    viewModel.playText(msg.text, preferredAccent)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.VolumeUp,
                                                    contentDescription = "استماع",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "استمع للرد",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isRoleplayLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }

                    // Bottom message input with voice or send
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 75.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userChatInput,
                            onValueChange = { userChatInput = it },
                            placeholder = { Text("اكتب ردك بالإنجليزي...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (userChatInput.isNotBlank()) {
                                    viewModel.sendRoleplayMessage(selectedScenario, userChatInput)
                                    userChatInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
                        }
                    }
                }
            }

            "WRITING" -> {
                // WRITING MODULE: SENTENCE CORRECTION & ESSAY FEEDBACK
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "مدقق الكتابة وتصحيح الجمل بالذكاء الاصطناعي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "اكتب أي جملة أو فقرة لتحصل على تصحيح فوري، درجة التقييم، وتنبيه بالأخطاء الشائعة.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = userWritingInput,
                                    onValueChange = { userWritingInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("writing_input_box"),
                                    label = { Text("نصك باللغة الإنجليزية") },
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.evaluateWriting(
                                            "ESL Writing Improvement",
                                            userWritingInput
                                        )
                                    },
                                    enabled = !isEvaluatingWriting && userWritingInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isEvaluatingWriting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جارِ التدقيق والتقييم...")
                                    } else {
                                        Icon(Icons.Default.Spellcheck, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تدقيق الجملة وتصحيح الأخطاء")
                                    }
                                }

                                AnimatedVisibility(visible = writingEval != null) {
                                    writingEval?.let { eval ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "نتيجة التقييم:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Surface(
                                                    color = if (eval.score >= 80) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "${eval.score}/100",
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (eval.score >= 80) Color(0xFF15803D) else Color(0xFFB45309),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "الصياغة الصحيحة المقترحة:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = eval.correctedText,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "الأخطاء المرصودة:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            eval.grammaticalErrors.forEach { err ->
                                                Text(text = "• $err", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "نصائح التطوير البلاغي:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            eval.enhancementTips.forEach { tip ->
                                                Text(text = "✓ $tip", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
