package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cefrLevel: String, // A1, A2, B1, B2, C1, C2
    val title: String,
    val arabicTitle: String,
    val grammarFocus: String,
    val explanationEn: String,
    val explanationAr: String,
    val examplesJson: String, // format: "English|||Arabic###English2|||Arabic2"
    val quizQuestionsJson: String, // format: "Question?|||OptA|||OptB|||OptC|||CorrectIndex|||Explanation"
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "vocabulary")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val phonetic: String,
    val arabicMeaning: String,
    val partOfSpeech: String, // noun, verb, adj, adv, idiom
    val exampleSentence: String,
    val exampleArabic: String,
    val category: String, // DAILY, BUSINESS, TRAVEL, ACADEMIC, TECH
    val cefrLevel: String, // A1..C2
    val isMastered: Boolean = false
)

@Entity(tableName = "srs_cards")
data class SrsCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vocabularyId: Int,
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val lastReviewTimestamp: Long = 0L,
    val state: String = "NEW" // NEW, LEARNING, REVIEW, MASTERED
)

@Entity(tableName = "ipa_sounds")
data class IpaSound(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String, // e.g. /i:/, /ɪ/, /θ/
    val name: String,
    val arabicName: String,
    val soundType: String, // VOWEL, DIPHTHONG, CONSONANT
    val usExampleWord: String,
    val ukExampleWord: String,
    val usPronunciationNotes: String,
    val ukPronunciationNotes: String,
    val articulationGuideAr: String,
    val minimalPairWordA: String,
    val minimalPairWordB: String,
    val minimalPairDifference: String
)

@Entity(tableName = "skill_exercises")
data class SkillExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val skillType: String, // LISTENING, READING, SPEAKING, WRITING
    val cefrLevel: String,
    val title: String,
    val arabicTitle: String,
    val contentEn: String,
    val contentArSummary: String,
    val questionsJson: String, // Questions or prompts
    val isCompleted: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val currentLevel: String = "A1",
    val xp: Int = 120,
    val streakDays: Int = 3,
    val lastActiveDate: String = "",
    val placementDone: Boolean = false,
    val preferredAccent: String = "US" // US or UK
)
