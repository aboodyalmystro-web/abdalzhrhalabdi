package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SrsCardWithVocab
import com.example.data.model.Vocabulary
import com.example.data.repository.SrsRating
import com.example.ui.components.AccentSelector
import com.example.ui.components.AudioPlayIconButton
import com.example.ui.components.CefrLevelBadge
import com.example.ui.components.PronunciationPracticeCard
import com.example.ui.viewmodel.EnglishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabSrsScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val dueCards by viewModel.dueSrsCards.collectAsState()
    val allCards by viewModel.allSrsCards.collectAsState()
    val activeDeck = if (dueCards.isNotEmpty()) dueCards else allCards
    val currentIndex by viewModel.currentSrsIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()
    val preferredAccent by viewModel.preferredAccent.collectAsState()

    val allVocab by viewModel.filteredVocabulary.collectAsState()
    val selectedCategory by viewModel.selectedVocabCategory.collectAsState()

    var currentViewMode by remember { mutableStateOf("FLASHCARDS") } // FLASHCARDS or LIST
    var practiceSpeechWord by remember { mutableStateOf<String?>(null) }

    val categories = listOf("ALL", "DAILY", "BUSINESS", "TRAVEL", "ACADEMIC", "TECH")
    val categoryNamesAr = mapOf(
        "ALL" to "الكل",
        "DAILY" to "حياة يومية",
        "BUSINESS" to "أعمال وشركات",
        "TRAVEL" to "سفر وسياحة",
        "ACADEMIC" to "أكاديميا",
        "TECH" to "تكنولوجيا"
    )

    val isListening by viewModel.speechHelper.isListening.collectAsState()
    val speechResult by viewModel.speechHelper.result.collectAsState()
    val speechError by viewModel.speechHelper.errorMessage.collectAsState()

    val safeIndex = if (activeDeck.isNotEmpty()) currentIndex.coerceIn(0, activeDeck.size - 1) else 0
    val currentCardWithVocab = activeDeck.getOrNull(safeIndex)

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
                    text = "بنك المفردات والتكرار المتباعد",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Spaced Repetition System (SRS)",
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

        // Mode switch tabs (Flashcards vs All Vocab List)
        TabRow(
            selectedTabIndex = if (currentViewMode == "FLASHCARDS") 0 else 1,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = currentViewMode == "FLASHCARDS",
                onClick = { currentViewMode = "FLASHCARDS" },
                text = { Text("البطاقات الاستذكارية (SRS)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = currentViewMode == "LIST",
                onClick = { currentViewMode = "LIST" },
                text = { Text("قاموس الكلمات (${allVocab.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (currentViewMode == "FLASHCARDS") {
            if (currentCardWithVocab != null) {
                val vocab = currentCardWithVocab.vocabulary
                val card = currentCardWithVocab.card

                // SRS Progress Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "البطاقة ${safeIndex + 1} من ${activeDeck.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (card.state) {
                            "MASTERED" -> Color(0xFFDCFCE7)
                            "REVIEW" -> Color(0xFFE0E7FF)
                            else -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Text(
                            text = when (card.state) {
                                "MASTERED" -> "متقنة ✨"
                                "REVIEW" -> "مراجعة (فترة ${card.intervalDays}d)"
                                else -> "بطاقة جديدة"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (card.state) {
                                "MASTERED" -> Color(0xFF15803D)
                                "REVIEW" -> Color(0xFF3730A3)
                                else -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Flashcard Box with Flip animation
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "cardFlip"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { viewModel.flipCard() }
                        .testTag("srs_flashcard"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rotation > 90f) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            // FRONT (English Word, Phonetic, Example)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CefrLevelBadge(level = vocab.cefrLevel)
                                    Text(
                                        text = vocab.partOfSpeech,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = vocab.word,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = vocab.phonetic,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AudioPlayIconButton(
                                        textToSpeak = vocab.word,
                                        onPlay = { txt, _ -> viewModel.playText(txt, preferredAccent) }
                                    )

                                    FilledTonalButton(
                                        onClick = { practiceSpeechWord = vocab.word },
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تدريب النطق", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${vocab.exampleSentence}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(10.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "👆 انقر على البطاقة لرؤية المعنى والتفاصيل",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // BACK (Arabic Meaning, Category, Translation)
                            Column(
                                modifier = Modifier.graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = categoryNamesAr[vocab.category] ?: vocab.category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = vocab.arabicMeaning,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "ترجمة المثال:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = vocab.exampleArabic,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "قيّم تذكرك للكلمة لتحديث جدول التكرار المتباعد:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SRS Rating Buttons (SM-2: Again, Hard, Good, Easy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Again
                    Button(
                        onClick = { viewModel.rateCard(currentCardWithVocab, SrsRating.AGAIN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("نسيت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("<10m", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Hard
                    Button(
                        onClick = { viewModel.rateCard(currentCardWithVocab, SrsRating.HARD) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("صعب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("1d", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Good
                    Button(
                        onClick = { viewModel.rateCard(currentCardWithVocab, SrsRating.GOOD) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("جيد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("3d", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Easy
                    Button(
                        onClick = { viewModel.rateCard(currentCardWithVocab, SrsRating.EASY) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("سهل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("7d", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                // Speech Practice Card for Current Flashcard
                if (practiceSpeechWord != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PronunciationPracticeCard(
                        targetText = practiceSpeechWord!!,
                        accent = preferredAccent,
                        isListening = isListening,
                        result = speechResult,
                        errorMessage = speechError,
                        onStartListening = { viewModel.speechHelper.startListening(practiceSpeechWord!!, preferredAccent) },
                        onStopListening = { viewModel.speechHelper.stopListening() },
                        onPlayReference = { viewModel.playText(practiceSpeechWord!!, preferredAccent) }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد بطاقات للمراجعة حالياً، أحسنت!", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // VOCABULARY EXPLORER LIST
            Column(modifier = Modifier.fillMaxSize()) {
                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setVocabCategory(cat) },
                            label = { Text(categoryNamesAr[cat] ?: cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allVocab) { word ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CefrLevelBadge(level = word.cefrLevel)
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = word.word,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = word.phonetic,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = word.arabicMeaning,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = word.exampleSentence,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AudioPlayIconButton(
                                    textToSpeak = word.word,
                                    onPlay = { txt, _ -> viewModel.playText(txt, preferredAccent) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
