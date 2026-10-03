package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Lesson
import com.example.ui.components.AccentSelector
import com.example.ui.components.CefrLevelBadge
import com.example.ui.viewmodel.EnglishViewModel

@Composable
fun HomeScreen(
    viewModel: EnglishViewModel,
    onNavigateToPlacement: () -> Unit,
    onNavigateToVocab: () -> Unit,
    onNavigateToPhonetics: () -> Unit,
    onNavigateToSkills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val filteredLessons by viewModel.filteredLessons.collectAsState()
    val selectedFilter by viewModel.selectedLevelFilter.collectAsState()
    val preferredAccent by viewModel.preferredAccent.collectAsState()
    val activeLesson by viewModel.activeLesson.collectAsState()

    val cefrLevels = listOf("ALL", "A1", "A2", "B1", "B2", "C1", "C2")

    if (activeLesson != null) {
        LessonDetailScreen(
            lesson = activeLesson!!,
            preferredAccent = preferredAccent,
            onClose = { viewModel.closeLesson() },
            onPlayAudio = { text, acc -> viewModel.playText(text, acc) },
            onComplete = { viewModel.completeLesson(it) }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header with User Level, XP, Streak, and Accent Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_header_card"),
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
                                text = "تعلم اللغة الانكليزية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "مسار التعلم الشامل من A1 إلى C2",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AccentSelector(
                            selectedAccent = preferredAccent,
                            onAccentChanged = { viewModel.setPreferredAccent(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current Level
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "المستوى الحالي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = userProfile?.currentLevel ?: "A1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        // Daily Streak
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔥", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "السلسلة اليومية", fontSize = 10.sp, color = Color(0xFF78350F))
                                    Text(text = "${userProfile?.streakDays ?: 1} أيام", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF78350F))
                                }
                            }
                        }

                        // XP Points
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⭐", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "النقاط (XP)", fontSize = 10.sp, color = Color(0xFF14532D))
                                    Text(text = "${userProfile?.xp ?: 150}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF14532D))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Visual Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_cover),
                        contentDescription = "غلاف تطبيق تعلم اللغة الانكليزية",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x05000000), Color(0xCC0F172A)),
                                    startY = 240f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "دورة احترافية معتمدة",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تعلم اللغة الانكليزية من الصفر حتى الإتقان",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "قواعد CEFR، بطاقات تكرار متباعد SRS، صوتيات IPA، ومحادثة ذكية",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Diagnostic Placement Test Banner (if not completed or retake)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPlacement() }
                    .testTag("placement_test_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userProfile?.placementDone == true) "اختبار تحديد المستوى (مكتمل)" else "اختبار تحديد المستوى التشخيصي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "10 أسئلة لمعرفة مستواك الفعلي من A1 إلى C2",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // 4. Quick Shortcuts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vocab SRS
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToVocab() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🗂️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "فلاش كاردز SRS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "تكرار متباعد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // IPA Phonetics
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPhonetics() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🗣️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "شجرة IPA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "أمريكي وبريطاني", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 4 Skills
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSkills() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎯", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "المهارات 4", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "استماع وتحدث", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 5. Section Header & CEFR Level Filter Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مسار القواعد والدروس التفاعلية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredLessons.size} دروس",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cefrLevels) { lvl ->
                        val isSelected = selectedFilter == lvl
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setLevelFilter(lvl) },
                            label = { Text(if (lvl == "ALL") "الكل" else lvl) }
                        )
                    }
                }
            }
        }

        // 6. Lesson Cards List
        items(filteredLessons) { lesson ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openLesson(lesson) }
                    .testTag("lesson_card_${lesson.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CefrLevelBadge(level = lesson.cefrLevel)
                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lesson.arabicTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lesson.grammarFocus,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (lesson.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "مكتمل",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "فتح الدرس",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
