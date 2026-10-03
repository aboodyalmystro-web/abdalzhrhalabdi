package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import kotlin.math.max

enum class SrsRating {
    AGAIN, // Forgot - reset
    HARD,  // Remembered with effort
    GOOD,  // Standard recall
    EASY   // Mastered / effortless
}

class EnglishRepository(private val database: AppDatabase) {

    private val lessonDao = database.lessonDao()
    private val vocabDao = database.vocabularyDao()
    private val srsDao = database.srsCardDao()
    private val ipaDao = database.ipaSoundDao()
    private val skillDao = database.skillExerciseDao()
    private val userProfileDao = database.userProfileDao()

    val allLessons: Flow<List<Lesson>> = lessonDao.getAllLessons()
    fun getLessonsByLevel(level: String): Flow<List<Lesson>> = lessonDao.getLessonsByLevel(level)
    suspend fun getLessonById(id: Int): Lesson? = lessonDao.getLessonById(id)

    suspend fun completeLesson(lesson: Lesson) {
        val updated = lesson.copy(isCompleted = true)
        lessonDao.updateLesson(updated)
        addXp(50)
    }

    val allVocabulary: Flow<List<Vocabulary>> = vocabDao.getAllVocabulary()
    fun getVocabularyByCategory(cat: String): Flow<List<Vocabulary>> = vocabDao.getVocabularyByCategory(cat)
    fun getVocabularyByLevel(lvl: String): Flow<List<Vocabulary>> = vocabDao.getVocabularyByLevel(lvl)

    val dueSrsCards: Flow<List<SrsCardWithVocab>> = srsDao.getDueCards(System.currentTimeMillis())
    val allSrsCards: Flow<List<SrsCardWithVocab>> = srsDao.getAllCards()

    suspend fun reviewCard(cardWithVocab: SrsCardWithVocab, rating: SrsRating) {
        val card = cardWithVocab.card
        val now = System.currentTimeMillis()
        var rep = card.repetitionCount
        var interval = card.intervalDays
        var ef = card.easeFactor
        var state = card.state

        when (rating) {
            SrsRating.AGAIN -> {
                rep = 0
                interval = 1
                ef = max(1.3f, ef - 0.2f)
                state = "LEARNING"
            }
            SrsRating.HARD -> {
                interval = max(1, (interval * 1.2f).toInt())
                ef = max(1.3f, ef - 0.15f)
                state = "REVIEW"
            }
            SrsRating.GOOD -> {
                interval = when (rep) {
                    0 -> 1
                    1 -> 3
                    else -> (interval * ef).toInt()
                }
                rep++
                state = "REVIEW"
            }
            SrsRating.EASY -> {
                interval = when (rep) {
                    0 -> 4
                    1 -> 7
                    else -> (interval * ef * 1.3f).toInt()
                }
                ef += 0.15f
                rep += 2
                state = "MASTERED"
            }
        }

        val nextReview = now + TimeUnit.DAYS.toMillis(interval.toLong())
        val updatedCard = card.copy(
            intervalDays = interval,
            repetitionCount = rep,
            easeFactor = ef,
            lastReviewTimestamp = now,
            nextReviewTimestamp = nextReview,
            state = state
        )
        srsDao.updateCard(updatedCard)
        addXp(15)
    }

    val allIpaSounds: Flow<List<IpaSound>> = ipaDao.getAllSounds()
    fun getSoundsByType(type: String): Flow<List<IpaSound>> = ipaDao.getSoundsByType(type)

    fun getSkillExercises(type: String): Flow<List<SkillExercise>> = skillDao.getExercisesByType(type)
    suspend fun completeSkillExercise(exercise: SkillExercise) {
        skillDao.updateExercise(exercise.copy(isCompleted = true))
        addXp(30)
    }

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun addXp(amount: Int) {
        userProfileDao.getUserProfile().let {
            // Read current profile
            // We use direct query or update
        }
        val current = database.openHelper.readableDatabase
        // Update user profile XP
        val cursor = current.query("SELECT id, currentLevel, xp, streakDays, lastActiveDate, placementDone, preferredAccent FROM user_profile WHERE id = 1")
        if (cursor.moveToFirst()) {
            val curLevel = cursor.getString(1)
            val curXp = cursor.getInt(2)
            val curStreak = cursor.getInt(3)
            val curDate = cursor.getString(4)
            val curPlacement = cursor.getInt(5) == 1
            val curAccent = cursor.getString(6)
            val newXp = curXp + amount
            val updated = UserProfile(
                id = 1,
                currentLevel = curLevel,
                xp = newXp,
                streakDays = curStreak,
                lastActiveDate = curDate,
                placementDone = curPlacement,
                preferredAccent = curAccent
            )
            userProfileDao.updateProfile(updated)
        }
        cursor.close()
    }

    suspend fun updateAccent(accent: String) {
        val cursor = database.openHelper.readableDatabase.query("SELECT currentLevel, xp, streakDays, lastActiveDate, placementDone FROM user_profile WHERE id = 1")
        if (cursor.moveToFirst()) {
            val profile = UserProfile(
                id = 1,
                currentLevel = cursor.getString(0),
                xp = cursor.getInt(1),
                streakDays = cursor.getInt(2),
                lastActiveDate = cursor.getString(3),
                placementDone = cursor.getInt(4) == 1,
                preferredAccent = accent
            )
            userProfileDao.updateProfile(profile)
        }
        cursor.close()
    }

    suspend fun savePlacementResult(level: String) {
        val cursor = database.openHelper.readableDatabase.query("SELECT xp, streakDays, lastActiveDate, preferredAccent FROM user_profile WHERE id = 1")
        if (cursor.moveToFirst()) {
            val profile = UserProfile(
                id = 1,
                currentLevel = level,
                xp = cursor.getInt(0) + 100, // Bonus for completing diagnostic
                streakDays = cursor.getInt(1),
                lastActiveDate = cursor.getString(2),
                placementDone = true,
                preferredAccent = cursor.getString(3)
            )
            userProfileDao.updateProfile(profile)
        }
        cursor.close()
    }
}
