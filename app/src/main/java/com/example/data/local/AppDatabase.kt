package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE cefrLevel = :level ORDER BY orderIndex ASC")
    fun getLessonsByLevel(level: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Int): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getCount(): Int
}

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary")
    fun getAllVocabulary(): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE category = :category")
    fun getVocabularyByCategory(category: String): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE cefrLevel = :level")
    fun getVocabularyByLevel(level: String): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getVocabularyById(id: Int): Vocabulary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabList: List<Vocabulary>)

    @Update
    suspend fun updateVocabulary(vocab: Vocabulary)

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun getCount(): Int
}

data class SrsCardWithVocab(
    @Embedded val card: SrsCard,
    @Relation(
        parentColumn = "vocabularyId",
        entityColumn = "id"
    )
    val vocabulary: Vocabulary
)

@Dao
interface SrsCardDao {
    @Transaction
    @Query("SELECT * FROM srs_cards WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    fun getDueCards(currentTime: Long): Flow<List<SrsCardWithVocab>>

    @Transaction
    @Query("SELECT * FROM srs_cards ORDER BY id ASC")
    fun getAllCards(): Flow<List<SrsCardWithVocab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SrsCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<SrsCard>)

    @Update
    suspend fun updateCard(card: SrsCard)

    @Query("SELECT COUNT(*) FROM srs_cards")
    suspend fun getCount(): Int
}

@Dao
interface IpaSoundDao {
    @Query("SELECT * FROM ipa_sounds ORDER BY id ASC")
    fun getAllSounds(): Flow<List<IpaSound>>

    @Query("SELECT * FROM ipa_sounds WHERE soundType = :type")
    fun getSoundsByType(type: String): Flow<List<IpaSound>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSounds(sounds: List<IpaSound>)

    @Query("SELECT COUNT(*) FROM ipa_sounds")
    suspend fun getCount(): Int
}

@Dao
interface SkillExerciseDao {
    @Query("SELECT * FROM skill_exercises WHERE skillType = :skillType")
    fun getExercisesByType(skillType: String): Flow<List<SkillExercise>>

    @Query("SELECT * FROM skill_exercises WHERE id = :id")
    suspend fun getExerciseById(id: Int): SkillExercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<SkillExercise>)

    @Update
    suspend fun updateExercise(exercise: SkillExercise)

    @Query("SELECT COUNT(*) FROM skill_exercises")
    suspend fun getCount(): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Database(
    entities = [
        Lesson::class,
        Vocabulary::class,
        SrsCard::class,
        IpaSound::class,
        SkillExercise::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun srsCardDao(): SrsCardDao
    abstract fun ipaSoundDao(): IpaSoundDao
    abstract fun skillExerciseDao(): SkillExerciseDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "master_english_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
