package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CefrLevelBadge
import com.example.ui.viewmodel.EnglishViewModel

@Composable
fun AssessmentScreen(
    viewModel: EnglishViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val placementIndex by viewModel.placementIndex.collectAsState()
    val placementScore by viewModel.placementScore.collectAsState()
    val resultLevel by viewModel.placementResultLevel.collectAsState()

    val questions = viewModel.placementQuestions
    val currentQuestion = questions.getOrNull(placementIndex)

    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "التقييم والتحفيز (Gamification)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "مستواك الحالي: ${userProfile?.currentLevel ?: "A1"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        CefrLevelBadge(level = userProfile?.currentLevel ?: "A1")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔥 ${userProfile?.streakDays ?: 1} أيام", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF78350F))
                                Text("سلسلة التعلم", fontSize = 11.sp, color = Color(0xFF78350F))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("⭐ ${userProfile?.xp ?: 150} XP", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF14532D))
                                Text("مجموع النقاط", fontSize = 11.sp, color = Color(0xFF14532D))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏆 4 أوسمة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text("الإنجازات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Placement Test Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("placement_quiz_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختبار تحديد المستوى (CEFR Diagnostic Test)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "10 أسئلة متدرجة من A1 إلى C2 لتشخيص دقيق لمهاراتك اللغوية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (resultLevel != null) {
                        // Result Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉 مبروك! اكتمل اختبار تحديد المستوى", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF15803D))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "مستواك المشخص: $resultLevel",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "نتيجتك: $placementScore من 10 | تم إضافة +100 XP لرصيدك",
                                fontSize = 13.sp,
                                color = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.resetPlacementTest() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                            ) {
                                Text("إعادة الاختبار لتحديث المستوى")
                            }
                        }
                    } else if (currentQuestion != null) {
                        // Test in progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السؤال ${placementIndex + 1} من ${questions.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            CefrLevelBadge(level = currentQuestion.targetLevel)
                        }

                        LinearProgressIndicator(
                            progress = { (placementIndex + 1).toFloat() / questions.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.questionEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentQuestion.questionAr,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Options
                        currentQuestion.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedOption == idx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedOption = idx
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
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
                                        onClick = { selectedOption = idx }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = opt, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedOption != null) {
                                    val isCorrect = selectedOption == currentQuestion.correctIndex
                                    viewModel.answerPlacementQuestion(isCorrect)
                                    selectedOption = null
                                }
                            },
                            enabled = selectedOption != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (placementIndex == questions.size - 1) "إنهاء الاختبار ومعرفة المستوى" else "السؤال التالي")
                        }
                    }
                }
            }
        }

        // Achievements & Badges List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الأوسمة والإنجازات المكتسبة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val badges = listOf(
                        Triple("🎯 الانطلاقة الأولى", "أتممت أول درس واختبار بنجاح", true),
                        Triple("🗣️ ناطق فصيح", "سجلت نطقاً بدقة تتجاوز 85% عبر الميكروفون", true),
                        Triple("📚 خبير المفردات", "راجعت 10 بطاقات في نظام التكرار المتباعد", true),
                        Triple("🔥 مداوم نشيط", "أكملت 3 أيام متتالية في التطبيق", true),
                        Triple("👑 محترف C2", "أتقنت جميع قواعد المستويات المتقدمة", false)
                    )

                    badges.forEach { (name, desc, unlocked) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (unlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (unlocked) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (unlocked) Color(0xFF15803D) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
