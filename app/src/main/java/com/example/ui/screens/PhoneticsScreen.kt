package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IpaSound
import com.example.ui.components.AccentSelector
import com.example.ui.components.AudioPlayIconButton
import com.example.ui.components.PronunciationPracticeCard
import com.example.ui.viewmodel.EnglishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneticsScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val filteredSounds by viewModel.filteredIpaSounds.collectAsState()
    val selectedFilter by viewModel.selectedIpaFilter.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val preferredAccent by viewModel.preferredAccent.collectAsState()

    val isListening by viewModel.speechHelper.isListening.collectAsState()
    val speechResult by viewModel.speechHelper.result.collectAsState()
    val speechError by viewModel.speechHelper.errorMessage.collectAsState()

    var testMinimalPairWord by remember { mutableStateOf<String?>(null) }

    val soundTypes = listOf("ALL", "VOWEL", "DIPHTHONG", "CONSONANT")
    val typeNamesAr = mapOf(
        "ALL" to "جميع الأصوات",
        "VOWEL" to "أصوات العلة (Vowels)",
        "DIPHTHONG" to "المركبة (Diphthongs)",
        "CONSONANT" to "الساكنة (Consonants)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الصوتيات ومخارج الحروف (IPA)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "مقارنة النطق الأمريكي (GA) والبريطاني (RP)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AccentSelector(
                    selectedAccent = preferredAccent,
                    onAccentChanged = { viewModel.setPreferredAccent(it) }
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(soundTypes) { type ->
                    val isSelected = selectedFilter == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setIpaFilter(type) },
                        label = { Text(typeNamesAr[type] ?: type) }
                    )
                }
            }
        }

        // Interactive IPA Sound Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "انقر على الرمز الصوتي للاستماع والمقارنة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid of sounds
                    val rows = filteredSounds.chunked(4)
                    rows.forEach { rowSounds ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSounds.forEach { sound ->
                                val isSelected = selectedSound?.id == sound.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            viewModel.selectSound(sound)
                                            // Speak primary example word
                                            val wordToSpeak = sound.usExampleWord.split(",").firstOrNull()?.trim() ?: sound.symbol
                                            viewModel.playText(wordToSpeak, preferredAccent)
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = sound.symbol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sound.soundType.take(3),
                                            fontSize = 9.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            // Filler if last row has fewer than 4
                            if (rowSounds.size < 4) {
                                repeat(4 - rowSounds.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Sound Deep Dive Card
        val sound = selectedSound ?: filteredSounds.firstOrNull()
        if (sound != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sound_details_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = sound.symbol,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = sound.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = sound.arabicName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // US vs UK Audio Comparison Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "مقارنة النطق الصوتي (US vs UK):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // American Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "🇺🇸 النطق الأمريكي (General American)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = sound.usExampleWord, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = sound.usPronunciationNotes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    AudioPlayIconButton(
                                        textToSpeak = sound.usExampleWord.split(",").firstOrNull()?.trim() ?: sound.usExampleWord,
                                        onPlay = { txt, _ -> viewModel.playText(txt, "US") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // British Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "🇬🇧 النطق البريطاني (Received Pronunciation)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = sound.ukExampleWord, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = sound.ukPronunciationNotes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    AudioPlayIconButton(
                                        textToSpeak = sound.ukExampleWord.split(",").firstOrNull()?.trim() ?: sound.ukExampleWord,
                                        onPlay = { txt, _ -> viewModel.playText(txt, "UK") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Articulation Guide in Arabic
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "دليل مخارج الصوت وحركة اللسان:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sound.articulationGuideAr,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Minimal Pairs Section
                        if (sound.minimalPairWordA.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "الأزواج الصغرى للتفريق السمعي (Minimal Pairs):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Word A
                                        OutlinedCard(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val wordClean = sound.minimalPairWordA.split("/").firstOrNull()?.trim() ?: sound.minimalPairWordA
                                                    testMinimalPairWord = wordClean
                                                    viewModel.playText(wordClean, preferredAccent)
                                                },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = sound.minimalPairWordA, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Word B
                                        OutlinedCard(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val wordClean = sound.minimalPairWordB.split("/").firstOrNull()?.trim() ?: sound.minimalPairWordB
                                                    testMinimalPairWord = wordClean
                                                    viewModel.playText(wordClean, preferredAccent)
                                                },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = sound.minimalPairWordB, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sound.minimalPairDifference,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Speech Evaluation tool
                        val practiceTarget = testMinimalPairWord ?: sound.usExampleWord.split(",").firstOrNull()?.trim() ?: "Sheep"
                        Spacer(modifier = Modifier.height(14.dp))

                        PronunciationPracticeCard(
                            targetText = practiceTarget,
                            accent = preferredAccent,
                            isListening = isListening,
                            result = speechResult,
                            errorMessage = speechError,
                            onStartListening = { viewModel.speechHelper.startListening(practiceTarget, preferredAccent) },
                            onStopListening = { viewModel.speechHelper.stopListening() },
                            onPlayReference = { viewModel.playText(practiceTarget, preferredAccent) }
                        )
                    }
                }
            }
        }
    }
}
