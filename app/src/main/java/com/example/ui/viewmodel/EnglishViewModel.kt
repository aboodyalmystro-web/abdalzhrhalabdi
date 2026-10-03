package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.SrsCardWithVocab
import com.example.data.model.*
import com.example.data.repository.EnglishRepository
import com.example.data.repository.SrsRating
import com.example.service.AiTutorService
import com.example.service.AudioService
import com.example.service.ChatMessage
import com.example.service.SpeechRecognitionHelper
import com.example.service.WritingEvaluation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlacementQuestion(
    val id: Int,
    val targetLevel: String,
    val questionEn: String,
    val questionAr: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationAr: String
)

class EnglishViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EnglishRepository(db)
    val audioService = AudioService(application)
    val speechHelper = SpeechRecognitionHelper(application)
    val aiTutor = AiTutorService()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedLevelFilter = MutableStateFlow("ALL")
    val selectedLevelFilter: StateFlow<String> = _selectedLevelFilter

    val filteredLessons: StateFlow<List<Lesson>> = combine(allLessons, _selectedLevelFilter) { list, filter ->
        if (filter == "ALL") list else list.filter { it.cefrLevel == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeLesson = MutableStateFlow<Lesson?>(null)
    val activeLesson: StateFlow<Lesson?> = _activeLesson

    val allVocabulary: StateFlow<List<Vocabulary>> = repository.allVocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVocabCategory = MutableStateFlow("ALL")
    val selectedVocabCategory: StateFlow<String> = _selectedVocabCategory

    val filteredVocabulary: StateFlow<List<Vocabulary>> = combine(allVocabulary, _selectedVocabCategory) { list, cat ->
        if (cat == "ALL") list else list.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueSrsCards: StateFlow<List<SrsCardWithVocab>> = repository.dueSrsCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSrsCards: StateFlow<List<SrsCardWithVocab>> = repository.allSrsCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSrsIndex = MutableStateFlow(0)
    val currentSrsIndex: StateFlow<Int> = _currentSrsIndex

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped

    val allIpaSounds: StateFlow<List<IpaSound>> = repository.allIpaSounds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIpaFilter = MutableStateFlow("ALL") // ALL, VOWEL, DIPHTHONG, CONSONANT
    val selectedIpaFilter: StateFlow<String> = _selectedIpaFilter

    val filteredIpaSounds: StateFlow<List<IpaSound>> = combine(allIpaSounds, _selectedIpaFilter) { list, filter ->
        if (filter == "ALL") list else list.filter { it.soundType == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSound = MutableStateFlow<IpaSound?>(null)
    val selectedSound: StateFlow<IpaSound?> = _selectedSound

    private val _preferredAccent = MutableStateFlow("US")
    val preferredAccent: StateFlow<String> = _preferredAccent

    // Four Skills State
    private val _activeSkillType = MutableStateFlow("LISTENING")
    val activeSkillType: StateFlow<String> = _activeSkillType

    val listeningExercises: StateFlow<List<SkillExercise>> = repository.getSkillExercises("LISTENING")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingExercises: StateFlow<List<SkillExercise>> = repository.getSkillExercises("READING")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speakingExercises: StateFlow<List<SkillExercise>> = repository.getSkillExercises("SPEAKING")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val writingExercises: StateFlow<List<SkillExercise>> = repository.getSkillExercises("WRITING")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Roleplay Chat
    private val _roleplayMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "Hello! I am your AI Speaking Partner. Select a scenario or start talking to practice real-world fluency!",
                arabicTranslation = "مرحباً بك! أنا شريكك الذكي لممارسة المحادثة. اختر سيناريو وابدأ التحدث لاكتساب الطلاقة!"
            )
        )
    )
    val roleplayMessages: StateFlow<List<ChatMessage>> = _roleplayMessages

    private val _isRoleplayLoading = MutableStateFlow(false)
    val isRoleplayLoading: StateFlow<Boolean> = _isRoleplayLoading

    // Writing evaluation
    private val _writingEvaluation = MutableStateFlow<WritingEvaluation?>(null)
    val writingEvaluation: StateFlow<WritingEvaluation?> = _writingEvaluation

    private val _isEvaluatingWriting = MutableStateFlow(false)
    val isEvaluatingWriting: StateFlow<Boolean> = _isEvaluatingWriting

    // Placement Test State
    private val _placementIndex = MutableStateFlow(0)
    val placementIndex: StateFlow<Int> = _placementIndex

    private val _placementScore = MutableStateFlow(0)
    val placementScore: StateFlow<Int> = _placementScore

    private val _placementResultLevel = MutableStateFlow<String?>(null)
    val placementResultLevel: StateFlow<String?> = _placementResultLevel

    init {
        viewModelScope.launch {
            DatabaseInitializer.populateInitialData(db)
        }
    }

    fun setLevelFilter(level: String) {
        _selectedLevelFilter.value = level
    }

    fun openLesson(lesson: Lesson) {
        _activeLesson.value = lesson
    }

    fun closeLesson() {
        _activeLesson.value = null
    }

    fun completeLesson(lesson: Lesson) {
        viewModelScope.launch {
            repository.completeLesson(lesson)
        }
    }

    fun setVocabCategory(cat: String) {
        _selectedVocabCategory.value = cat
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun rateCard(cardWithVocab: SrsCardWithVocab, rating: SrsRating) {
        viewModelScope.launch {
            repository.reviewCard(cardWithVocab, rating)
            _isCardFlipped.value = false
            // Move to next card
            val next = _currentSrsIndex.value + 1
            val cardsList = dueSrsCards.value.ifEmpty { allSrsCards.value }
            if (next < cardsList.size) {
                _currentSrsIndex.value = next
            } else {
                _currentSrsIndex.value = 0
            }
        }
    }

    fun nextCard() {
        _isCardFlipped.value = false
        val cardsList = dueSrsCards.value.ifEmpty { allSrsCards.value }
        if (cardsList.isNotEmpty()) {
            _currentSrsIndex.value = (_currentSrsIndex.value + 1) % cardsList.size
        }
    }

    fun setIpaFilter(filter: String) {
        _selectedIpaFilter.value = filter
    }

    fun selectSound(sound: IpaSound?) {
        _selectedSound.value = sound
    }

    fun setPreferredAccent(accent: String) {
        _preferredAccent.value = accent
        audioService.currentAccent = accent
        viewModelScope.launch {
            repository.updateAccent(accent)
        }
    }

    fun playText(text: String, accent: String? = null, rate: Float? = null) {
        audioService.speak(text, accent ?: _preferredAccent.value, rate)
    }

    fun setActiveSkillType(type: String) {
        _activeSkillType.value = type
    }

    fun sendRoleplayMessage(scenario: String, messageText: String) {
        if (messageText.isBlank()) return
        val userMsg = ChatMessage(sender = "USER", text = messageText)
        _roleplayMessages.value = _roleplayMessages.value + userMsg

        viewModelScope.launch {
            _isRoleplayLoading.value = true
            val aiResponse = aiTutor.sendRoleplayMessage(scenario, messageText, _roleplayMessages.value)
            _roleplayMessages.value = _roleplayMessages.value + aiResponse
            _isRoleplayLoading.value = false
            // Auto play tutor audio in target accent
            playText(aiResponse.text)
        }
    }

    fun clearRoleplayChat(scenario: String) {
        _roleplayMessages.value = listOf(
            ChatMessage(
                sender = "AI",
                text = "Welcome to the $scenario session. How can I assist you today?",
                arabicTranslation = "مرحباً بك في جلسة $scenario. كيف يمكنني مساعدتك اليوم؟"
            )
        )
    }

    fun evaluateWriting(prompt: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isEvaluatingWriting.value = true
            val eval = aiTutor.evaluateWriting(prompt, text)
            _writingEvaluation.value = eval
            _isEvaluatingWriting.value = false
            repository.addXp(35)
        }
    }

    // Diagnostic Placement Test
    val placementQuestions = listOf(
        PlacementQuestion(
            id = 1,
            targetLevel = "A1",
            questionEn = "My sister ___ twenty years old.",
            questionAr = "أختي تبلغ من العمر عشرين عاماً.",
            options = listOf("am", "is", "are", "have"),
            correctIndex = 1,
            explanationAr = "مع الفاعل المفرد الغائب (My sister = She) نستخدم is."
        ),
        PlacementQuestion(
            id = 2,
            targetLevel = "A2",
            questionEn = "Where ___ you go on vacation last summer?",
            questionAr = "أين ذهبت في عطلتك الصيف الماضي؟",
            options = listOf("do", "did", "have", "were"),
            correctIndex = 1,
            explanationAr = "في السؤال في زمن الماضي البسيط مع فعل أساسي (go) نستخدم Did."
        ),
        PlacementQuestion(
            id = 3,
            targetLevel = "B1",
            questionEn = "I haven't seen Omar ___ we graduated from university.",
            questionAr = "لم أرَ عمر منذ أن تخرجنا من الجامعة.",
            options = listOf("for", "since", "during", "ago"),
            correctIndex = 1,
            explanationAr = "نستخدم since مع نقطة بداية حدث بالماضي مرتبط بالحاضر."
        ),
        PlacementQuestion(
            id = 4,
            targetLevel = "B1",
            questionEn = "If I had more time, I ___ learn another language.",
            questionAr = "لو كان لدي مزيد من الوقت، لتعلمت لغة أخرى.",
            options = listOf("will", "would", "must", "can"),
            correctIndex = 1,
            explanationAr = "الحالة الشرطية الثانية (افتراض حاضر غير حقيقي) تتطلب would + المصدر."
        ),
        PlacementQuestion(
            id = 5,
            targetLevel = "B2",
            questionEn = "Despite ___ very hard, she didn't win first place.",
            questionAr = "رغم محاولتها الجادة جداً، لم تفز بالمركز الأول.",
            options = listOf("try", "to try", "trying", "tried"),
            correctIndex = 2,
            explanationAr = "بعد حرف الجر Despite يأتي اسم أو فعل مضاف له -ing (Gerund)."
        ),
        PlacementQuestion(
            id = 6,
            targetLevel = "B2",
            questionEn = "By next month, our engineering team ___ the new mobile architecture.",
            questionAr = "بحلول الشهر القادم، سيكون فريقنا الهندسي قد أتم بناء المعمارية الجديدة.",
            options = listOf("finishes", "will finish", "will have finished", "had finished"),
            correctIndex = 2,
            explanationAr = "المستقبل التام (will have finished) يعبر عن حدث سيكتمل قبل نقطة زمنية مستقبلية (By next month)."
        ),
        PlacementQuestion(
            id = 7,
            targetLevel = "C1",
            questionEn = "Seldom ___ such an articulate explanation of quantum mechanics.",
            questionAr = "نادراً ما سمعت شرحاً بليغاً ومتقناً كهذا لميكانيكا الكم.",
            options = listOf("I have heard", "have I heard", "did I heard", "I had heard"),
            correctIndex = 1,
            explanationAr = "القلب النحوي (Inversion) بعد كلمة Seldom: يتقدم الفعل المساعد have على الفاعل I."
        ),
        PlacementQuestion(
            id = 8,
            targetLevel = "C1",
            questionEn = "The contract was null and void owing ___ a procedural breach.",
            questionAr = "اعتُبر العقد باطلاً ولاغياً بسبب خرق إجرائي.",
            options = listOf("for", "by", "to", "with"),
            correctIndex = 2,
            explanationAr = "التعبير الاصطلاحي الصحيح للسببية هو owing to (بسبب / نظراً لـ)."
        ),
        PlacementQuestion(
            id = 9,
            targetLevel = "C2",
            questionEn = "It is paramount that the delegate ___ immediate counsel before signing.",
            questionAr = "من بالغ الأهمية أن يلتمس المندوب مشورة فورية قبل التوقيع.",
            options = listOf("seeks", "seek", "sought", "is seeking"),
            correctIndex = 1,
            explanationAr = "صيغة Subjunctive بعد صفة paramount تتطلب الفعل مجرداً (seek) دون s."
        ),
        PlacementQuestion(
            id = 10,
            targetLevel = "C2",
            questionEn = "Were you ___ to accept the fellowship, what would be your primary research scope?",
            questionAr = "لو قُدّر لك قبول الزمالة، فماذا سيكون نطاق بحثك الأساسي؟",
            options = listOf("to", "in order", "supposed", "likely"),
            correctIndex = 0,
            explanationAr = "تركيب 'Were you to accept' هو الصيغة البلاغية الراقية المكافئة لـ 'If you were to accept'."
        )
    )

    fun answerPlacementQuestion(isCorrect: Boolean) {
        if (isCorrect) {
            _placementScore.value += 1
        }
        val next = _placementIndex.value + 1
        if (next < placementQuestions.size) {
            _placementIndex.value = next
        } else {
            // Calculate final CEFR Level
            val score = _placementScore.value
            val assignedLevel = when {
                score <= 2 -> "A1"
                score <= 4 -> "A2"
                score <= 6 -> "B1"
                score <= 8 -> "B2"
                score == 9 -> "C1"
                else -> "C2"
            }
            _placementResultLevel.value = assignedLevel
            viewModelScope.launch {
                repository.savePlacementResult(assignedLevel)
            }
        }
    }

    fun resetPlacementTest() {
        _placementIndex.value = 0
        _placementScore.value = 0
        _placementResultLevel.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioService.shutdown()
        speechHelper.stopListening()
    }
}
