package com.example.data.local

import com.example.data.model.*

object DatabaseInitializer {

    suspend fun populateInitialData(database: AppDatabase) {
        val lessonDao = database.lessonDao()
        val vocabDao = database.vocabularyDao()
        val srsDao = database.srsCardDao()
        val ipaDao = database.ipaSoundDao()
        val skillDao = database.skillExerciseDao()
        val profileDao = database.userProfileDao()

        if (lessonDao.getCount() == 0) {
            lessonDao.insertLessons(initialLessons)
        }

        if (vocabDao.getCount() == 0) {
            vocabDao.insertVocabulary(initialVocabulary)
        }

        if (srsDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val initialCards = (1..initialVocabulary.size).map { vocabId ->
                SrsCard(
                    vocabularyId = vocabId,
                    intervalDays = 1,
                    repetitionCount = 0,
                    easeFactor = 2.5f,
                    nextReviewTimestamp = now, // Ready for review right away
                    state = "NEW"
                )
            }
            srsDao.insertCards(initialCards)
        }

        if (ipaDao.getCount() == 0) {
            ipaDao.insertSounds(initialIpaSounds)
        }

        if (skillDao.getCount() == 0) {
            skillDao.insertExercises(initialSkillExercises)
        }

        profileDao.insertProfile(
            UserProfile(
                id = 1,
                currentLevel = "A1",
                xp = 150,
                streakDays = 3,
                lastActiveDate = "2026-10-02",
                placementDone = false,
                preferredAccent = "US"
            )
        )
    }

    private val initialLessons = listOf(
        Lesson(
            cefrLevel = "A1",
            title = "Present Simple & 'To Be'",
            arabicTitle = "المضارع البسيط وفعل الكينونة (To Be)",
            grammarFocus = "Subject + Verb (s/es) | am/is/are",
            explanationEn = "The Present Simple expresses habits, facts, and daily routines. With he/she/it, add -s or -es to the verb. The verb 'to be' has three forms: am (I), is (he/she/it), and are (we/you/they).",
            explanationAr = "يُستخدم زمن المضارع البسيط للتعبير عن الحقائق العامة والعادات والروتين اليومي. مع الضمائر (he, she, it) نضيف s أو es للفعل. فعل الكينونة To Be ينقسم إلى (am مع I)، (is مع المفرد)، و (are مع الجمع والمخاطب). لا تنسَ أن الجملة الإنجليزية تتطلب فعلاً دائماً بعكس الجملة الاسمية في العربية.",
            examplesJson = "I am a passionate software engineer.|||أنا مهندس برمجيات شغوف.###She drinks coffee every morning.|||هي تشرب القهوة كل صباح.###They do not speak French.|||هم لا يتحدثون الفرنسية.###Where does he live?|||أين يعيش؟",
            quizQuestionsJson = "She ___ to school every day.|||go|||goes|||going|||1|||مع الضمير المفرد الغائب (She) نضيف -es للفعل go في المضارع البسيط.###___ you ready for the exam?|||Is|||Are|||Am|||1|||مع الضمير you نستخدم فعل الكينونة Are.",
            isCompleted = true,
            orderIndex = 1
        ),
        Lesson(
            cefrLevel = "A2",
            title = "Past Simple & Time Markers",
            arabicTitle = "الماضي البسيط والكلمات الدالة",
            grammarFocus = "Subject + Verb(ed) / Irregular Verb + yesterday/ago/last",
            explanationEn = "Use the Past Simple for actions completed at a specific time in the past. Regular verbs add -ed (walk -> walked), while irregular verbs have unique forms (go -> went, buy -> bought). In negatives and questions, use 'did' + base verb.",
            explanationAr = "يُستخدم الماضي البسيط لأحداث بدأت وانتهت في الماضي في وقت محدد. الأفعال المنتظمة تأخذ -ed، بينما الأفعال الشاذة تحفظ كما هي (مثل see -> saw). في النفي والسؤال نستخدم Did ويعود الفعل لأصله.",
            examplesJson = "We visited London last summer.|||زرنا لندن الصيف الماضي.###He didn't find his passport.|||لم يجد جواز سفره.###Did you see the new movie yesterday?|||هل شاهدت الفيلم الجديد أمس؟",
            quizQuestionsJson = "They ___ a new car two weeks ago.|||buy|||bought|||buying|||1|||الماضي من الفعل buy هو الفعل الشاذ bought.###Did you ___ the meeting?|||attended|||attend|||attending|||1|||بعد Did في السؤال يعود الفعل إلى المصدر المجرد attend.",
            isCompleted = false,
            orderIndex = 2
        ),
        Lesson(
            cefrLevel = "B1",
            title = "Present Perfect vs Past Simple",
            arabicTitle = "المضارع التام والفرق بينه وبين الماضي البسيط",
            grammarFocus = "have/has + Past Participle (V3) vs Past Simple",
            explanationEn = "Use Present Perfect when the past action connects to the present or the specific time is not mentioned (experiences, recent news, life achievements with 'ever', 'never', 'already', 'yet', 'since', 'for'). Use Past Simple when the exact time is stated.",
            explanationAr = "المضارع التام (have/has + التصريف الثالث) يربط الماضي بالحاضر؛ إما لخبرة حياتية دون تحديد زمن دقيق، أو لحدث ما زال أثره ملموساً. نستخدم معه (since, for, already, yet). أما إذا حُدد الزمن (yesterday, in 2020) فنستخدم الماضي البسيط حصراً.",
            examplesJson = "I have lived in Dubai for five years.|||أعيش في دبي منذ خمس سنوات (وما زلت أعيش هناك).###I moved to Dubai in 2021.|||انتقلت إلى دبي في 2021 (تحديد زمن بالماضي).###Have you ever visited Japan?|||هل سبق لك زيارة اليابان؟",
            quizQuestionsJson = "I have ___ my keys, can you help me look?|||lost|||lose|||losing|||0|||التصريف الثالث من lose هو lost بعد have.###He has worked here ___ 2018.|||for|||since|||ago|||1|||نستخدم since مع نقطة البداية الزمنية المحددة.",
            isCompleted = false,
            orderIndex = 3
        ),
        Lesson(
            cefrLevel = "B2",
            title = "Conditionals (Zero to Third)",
            arabicTitle = "الجمل الشرطية (من الصفرية إلى الحالة الثالثة)",
            grammarFocus = "If clauses: Type 0, 1, 2, and 3 conditionals",
            explanationEn = "Zero: Real facts (If + present, present). First: Real future possibilities (If + present, will + verb). Second: Hypothetical/imaginary present (If + past, would + verb). Third: Regret about the past (If + past perfect, would have + V3).",
            explanationAr = "الحالة الصفرية: حقائق علمية (If you heat water, it boils). الحالة الأولى: احتمال مستقبلي واقعي (If it rains, we will stay home). الحالة الثانية: افتراض وتخيل لواقع حاضر غير حقيقي (If I had wings, I would fly). الحالة الثالثة: ندم واستحالة في الماضي (If I had studied harder, I would have passed).",
            examplesJson = "If I won the lottery, I would travel the world.|||لو فزت باليانصيب، لسافرت حول العالم (حالة ثانية - افتراض).###If she had warned me, I wouldn't have made that mistake.|||لو كانت قد حذرتني، لما ارتكبت ذلك الخطأ (حالة ثالثة - ندم).",
            quizQuestionsJson = "If I ___ you, I would accept the job offer.|||am|||was|||were|||2|||في الحالة الشرطية الثانية الافتراضية نستخدم were مع جميع الضمائر بما فيها I.###If it rains tomorrow, we ___ the match.|||cancel|||will cancel|||would cancel|||1|||الحالة الشرطية الأولى تتطلب will + المصدر في جواب الشرط.",
            isCompleted = false,
            orderIndex = 4
        ),
        Lesson(
            cefrLevel = "C1",
            title = "Inversion & Emphatic Structures",
            arabicTitle = "القلب النحوي وتراكيب التوكيد البلاغي",
            grammarFocus = "Negative adverbials at the start + auxiliary + subject + verb",
            explanationEn = "Inversion occurs when negative or restrictive adverbs (Hardly, Seldom, Rarely, Under no circumstances, Not only) are placed at the beginning of a clause for dramatic emphasis. The word order mimics a question: Adverbial + Aux + Subject + Main Verb.",
            explanationAr = "القلب النحوي (Inversion) يُستخدم في الإنجليزية المتقدمة لإعطاء قوة وتأكيد بلاغي؛ حيث تبدأ الجملة بكلمة نفي أو تقييد (مثل Rarely, Never, Scarcely) ويتقدم الفعل المساعد على الفاعل تماماً كصيغة السؤال.",
            examplesJson = "Rarely have I seen such dedication.|||نادراً ما رأيت مثل هذا التفاني.###Under no circumstances should you share your password.|||تحت أي ظرف من الظروف لا ينبغي لك مشاركة كلمة مرورك.###Not only did he arrive late, but he also forgot the report.|||لم يقتصر الأمر على وصوله متأخراً، بل نسي التقرير أيضاً.",
            quizQuestionsJson = "Scarcely ___ started the presentation when the power went out.|||I had|||had I|||did I had|||1|||بعد Scarcely يتقدم الفعل المساعد had على الفاعل I.###Never before ___ such an incredible performance.|||we witnessed|||have we witnessed|||witnessed we|||1|||بعد Never before نستخدم صيغة القلب: have we witnessed.",
            isCompleted = false,
            orderIndex = 5
        ),
        Lesson(
            cefrLevel = "C2",
            title = "Subjunctive Mood & Nuanced Modality",
            arabicTitle = "صيغة الإلزام والافتراض الدقيق (Subjunctive Mood)",
            grammarFocus = "Mandative verbs (insist, demand, recommend) + that + bare infinitive",
            explanationEn = "The mandative subjunctive uses the bare infinitive of the verb regardless of the subject (even for he/she) after verbs/adjectives expressing demand, urgency, or recommendation (e.g., 'It is imperative that he be present', 'I suggest that she take the lead').",
            explanationAr = "صيغة Subjunctive تعبر عن الضرورة القصوى أو التوصية الرسمية بعد أفعال مثل (demand, insist, recommend, require) والصفات مثل (essential, crucial, imperative). يأتي الفعل بعدها في صيغة المصدر المجرد تماماً حتى مع ضمائر المفرد (بدون s وبدون to).",
            examplesJson = "The board demanded that the CEO resign immediately.|||طالب مجلس الإدارة بأن يستقيل الرئيس التنفيذي فوراً (لاحظ الفعل resign بدون s).###It is vital that every citizen be informed.|||من الحيوي أن يكون كل مواطن على علم ودراية.",
            quizQuestionsJson = "The doctor recommended that he ___ smoking immediately.|||quits|||quit|||quitted|||1|||في صيغة Subjunctive يظل الفعل مجرداً quit دون إضافة s.###It is imperative that she ___ on time.|||is|||be|||was|||1|||المصدر من فعل الكينونة هو be.",
            isCompleted = false,
            orderIndex = 6
        )
    )

    private val initialVocabulary = listOf(
        // DAILY
        Vocabulary(
            word = "Commute",
            phonetic = "/kəˈmjuːt/",
            arabicMeaning = "التنقل اليومي للعمل أو الدراسة",
            partOfSpeech = "noun / verb",
            exampleSentence = "My daily commute takes approximately forty-five minutes by subway.",
            exampleArabic = "يستغرق تنقلي اليومي حوالي خمس وأربعين دقيقة عبر المترو.",
            category = "DAILY",
            cefrLevel = "B1"
        ),
        Vocabulary(
            word = "Versatile",
            phonetic = "/ˈvɜːrsətl/",
            arabicMeaning = "متعدد الاستخدامات / مرن المهارات",
            partOfSpeech = "adjective",
            exampleSentence = "An air fryer is a versatile kitchen appliance for cooking healthy meals.",
            exampleArabic = "المقلاة الهوائية جهاز مطبخ متعدد الاستخدامات لطهي وجبات صحية.",
            category = "DAILY",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Procrastinate",
            phonetic = "/prəˈkræstɪneɪt/",
            arabicMeaning = "يماطل / يسوّف",
            partOfSpeech = "verb",
            exampleSentence = "Don't procrastinate on doing your chores before the weekend arrives.",
            exampleArabic = "لا تسوّف في إنجاز أعمالك المنزلية قبل حلول عطلة نهاية الأسبوع.",
            category = "DAILY",
            cefrLevel = "B2"
        ),

        // BUSINESS
        Vocabulary(
            word = "Leverage",
            phonetic = "/ˈlevərɪdʒ/",
            arabicMeaning = "يوظف / يستغل مورداً لأقصى فائدة",
            partOfSpeech = "verb / noun",
            exampleSentence = "We can leverage modern artificial intelligence to automate customer support.",
            exampleArabic = "يمكننا استغلال الذكاء الاصطناعي الحديث لأتمتة الدعم الفني للعملاء.",
            category = "BUSINESS",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Milestone",
            phonetic = "/ˈmaɪlstoʊn/",
            arabicMeaning = "إنجاز بارز / نقطة تحول هامة",
            partOfSpeech = "noun",
            exampleSentence = "Reaching one million active users is a monumental milestone for the startup.",
            exampleArabic = "الوصول إلى مليون مستخدم نشط يمثل محطة إنجاز بارزة للشركة الناشئة.",
            category = "BUSINESS",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Negotiate",
            phonetic = "/nəˈɡoʊʃieɪt/",
            arabicMeaning = "يتفاوض للوصول إلى اتفاق",
            partOfSpeech = "verb",
            exampleSentence = "The executives met to negotiate the final terms of the partnership contract.",
            exampleArabic = "اجتمع المسؤولون التنفيذيون للتفاوض حول البنود النهائية لعقد الشراكة.",
            category = "BUSINESS",
            cefrLevel = "B1"
        ),

        // TRAVEL
        Vocabulary(
            word = "Itinerary",
            phonetic = "/aɪˈtɪnəreri/",
            arabicMeaning = "جدول / خطة الرحلة التفصيلية",
            partOfSpeech = "noun",
            exampleSentence = "Our travel itinerary includes visiting historical museums and coastal landmarks.",
            exampleArabic = "يتضمن جدول رحلتنا زيارة المتاحف التاريخية والمعالم الساحلية.",
            category = "TRAVEL",
            cefrLevel = "B1"
        ),
        Vocabulary(
            word = "Picturesque",
            phonetic = "/ˌpɪktʃəˈresk/",
            arabicMeaning = "بديع المنظر كأنه لوحة فنية",
            partOfSpeech = "adjective",
            exampleSentence = "The train rode through picturesque alpine villages surrounded by green hills.",
            exampleArabic = "مرّ القطار عبر قرى جبلية بديعة المنظر محاطة بتلال خضراء.",
            category = "TRAVEL",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Hospitality",
            phonetic = "/ˌhɑːspɪˈtæləti/",
            arabicMeaning = "كرم الضيافة وحسن الاستقبال",
            partOfSpeech = "noun",
            exampleSentence = "The local residents welcomed travelers with genuine warmth and hospitality.",
            exampleArabic = "رحب السكان المحليون بالمسافرين بدفء وكرم ضيافة أصيل.",
            category = "TRAVEL",
            cefrLevel = "B1"
        ),

        // ACADEMIC
        Vocabulary(
            word = "Hypothesis",
            phonetic = "/haɪˈpɑːθəsɪs/",
            arabicMeaning = "فرضية علمية قابلة للاختبار",
            partOfSpeech = "noun",
            exampleSentence = "The research team conducted multiple trials to validate their scientific hypothesis.",
            exampleArabic = "أجرى فريق البحث تجارب متعددة للتحقق من صحة فرضيتهم العلمية.",
            category = "ACADEMIC",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Empirical",
            phonetic = "/ɪmˈpɪrɪkl/",
            arabicMeaning = "تجريبي مبني على الملاحظة والقياس",
            partOfSpeech = "adjective",
            exampleSentence = "Her doctoral thesis provides solid empirical evidence for cognitive linguistic theories.",
            exampleArabic = "تقدم أطروحة الدكتوراه الخاصة بها دليلاً تجريبياً ملموساً يدعم نظريات علم اللغة الإدراكي.",
            category = "ACADEMIC",
            cefrLevel = "C1"
        ),
        Vocabulary(
            word = "Coherent",
            phonetic = "/koʊˈhɪrənt/",
            arabicMeaning = "متماسك ومنطقي ومترابط",
            partOfSpeech = "adjective",
            exampleSentence = "Ensure your academic argument is structured in a clear and coherent manner.",
            exampleArabic = "احرص على أن تكون حجتك الأكاديمية مبنية بطريقة واضحة ومترابطة منطقياً.",
            category = "ACADEMIC",
            cefrLevel = "B2"
        ),

        // TECH
        Vocabulary(
            word = "Scalable",
            phonetic = "/ˈskeɪləbl/",
            arabicMeaning = "قابل للتوسع والنمو البرمجي",
            partOfSpeech = "adjective",
            exampleSentence = "Microservices architecture allows developers to build highly scalable backend systems.",
            exampleArabic = "تتيح بنية الخدمات المصغرة للمطورين بناء أنظمة خلفية عالية القابلية للتوسع.",
            category = "TECH",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Latency",
            phonetic = "/ˈleɪtnsi/",
            arabicMeaning = "زمن الاستجابة / التأخير في نقل البيانات",
            partOfSpeech = "noun",
            exampleSentence = "Optimizing database queries significantly reduced the API network latency.",
            exampleArabic = "أدى تحسين استعلامات قاعدة البيانات إلى تقليل زمن استجابة واجهة التطبيقات بشكل كبير.",
            category = "TECH",
            cefrLevel = "B2"
        ),
        Vocabulary(
            word = "Cryptography",
            phonetic = "/krɪpˈtɑːɡrəfi/",
            arabicMeaning = "علم التشفير وحماية البيانات",
            partOfSpeech = "noun",
            exampleSentence = "Public-key cryptography is the foundation of secure web transactions.",
            exampleArabic = "تشفير المفتاح العام هو حجر الأساس للتعاملات الآمنة عبر شبكة الإنترنت.",
            category = "TECH",
            cefrLevel = "C1"
        )
    )

    private val initialIpaSounds = listOf(
        // VOWELS (Monophthongs)
        IpaSound(
            symbol = "/i:/",
            name = "Long Close Front Vowel",
            arabicName = "ياء طويلة وممتدة",
            soundType = "VOWEL",
            usExampleWord = "Sheep, Feel, Believe",
            ukExampleWord = "Sheep, Feel, Believe",
            usPronunciationNotes = "Lips spread wide, tongue high and forward in the mouth.",
            ukPronunciationNotes = "Tense, identical vowel quality, slightly longer duration in RP.",
            articulationGuideAr = "افتح الشفتين بابتسامة عريضة وارفع مقدمة اللسان للأعلى قريباً من سقف الحلق.",
            minimalPairWordA = "Sheep /ʃi:p/",
            minimalPairWordB = "Ship /ʃɪp/",
            minimalPairDifference = "الفرق بين الياء الطويلة المشدودة والكسرة القصيرة"
        ),
        IpaSound(
            symbol = "/ɪ/",
            name = "Short Near-Close Vowel",
            arabicName = "كسرة إنجليزية قصيرة ومرتخية",
            soundType = "VOWEL",
            usExampleWord = "Ship, Sit, Myth",
            ukExampleWord = "Ship, Sit, Myth",
            usPronunciationNotes = "Relaxed tongue position, slightly lower than /i:/.",
            ukPronunciationNotes = "Very crisp and short.",
            articulationGuideAr = "تشبه الكسرة العربية المرتخية؛ لا تشد الشفتين كأنك تبتسم بل دعهما مسترخيتين.",
            minimalPairWordA = "Fit /fɪt/",
            minimalPairWordB = "Feet /fi:t/",
            minimalPairDifference = "Fit (مناسب) بكسرة خفيفة مقابل Feet (أقدام) بياء ممدودة"
        ),
        IpaSound(
            symbol = "/æ/",
            name = "Short Open Front Vowel (Trap Vowel)",
            arabicName = "ألف مرققة مفتوحة الفم (الفتحة العريضة)",
            soundType = "VOWEL",
            usExampleWord = "Cat, Flat, Apple",
            ukExampleWord = "Cat, Flat, Apple",
            usPronunciationNotes = "Jaw drops significantly, very open mouth, nasalized before /m/ and /n/.",
            ukPronunciationNotes = "Less nasalized, mouth slightly less wide.",
            articulationGuideAr = "افتح فكك للأسفل بوضوح مع إبقاء اللسان مستوياً في قاع الفم.",
            minimalPairWordA = "Bad /bæd/",
            minimalPairWordB = "Bed /bed/",
            minimalPairDifference = "Bad (سيئ) فتحة فم واسعة مقابل Bed (سرير) بكسرة/فتحة متوسطة"
        ),
        IpaSound(
            symbol = "/ə/",
            name = "Schwa (The Most Common English Sound)",
            arabicName = "صوت الشوا (الصوت المحايد الأشهر في الإنجليزية)",
            soundType = "VOWEL",
            usExampleWord = "About, Banana, Computer",
            ukExampleWord = "About, Banana, Computer",
            usPronunciationNotes = "Completely relaxed, central tongue position. Occurs only in unstressed syllables.",
            ukPronunciationNotes = "Universal neutral vowel in unaccented positions.",
            articulationGuideAr = "الصوت الأكثر تكراراً؛ الفم واللسان في حالة استرخاء تام دون أي جهد أو ضم أو مد.",
            minimalPairWordA = "Present /ˈpreznt/ (Noun)",
            minimalPairWordB = "Present /prɪˈzent/ (Verb)",
            minimalPairDifference = "تغيير مكان النبر والشوا يغير الكلمة من اسم إلى فعل"
        ),
        IpaSound(
            symbol = "/ɑ:/ vs /ɒ/",
            name = "Open Back Vowels (Major US vs UK difference)",
            arabicName = "صوت العلة الخلفي (فرق رئيسي بين الأمريكي والبريطاني)",
            soundType = "VOWEL",
            usExampleWord = "Hot, Bottle, Coffee -> pronounced /hɑːt/, /ˈbɑːtl/",
            ukExampleWord = "Hot, Bottle, Coffee -> pronounced /hɒt/, /ˈbɒtl/",
            usPronunciationNotes = "American English merges /ɒ/ into unrounded /ɑː/ (Father-Bother merger).",
            ukPronunciationNotes = "British English rounds the lips into a distinct short /ɒ/.",
            articulationGuideAr = "في الأمريكي يفتح الفم دون تدوير الشفتين كأنك تقول (هات)، بينما في البريطاني تدور الشفتان (هوت).",
            minimalPairWordA = "US: Box /bɑːks/",
            minimalPairWordB = "UK: Box /bɒks/",
            minimalPairDifference = "عدم استدارة الشفاه في النطق الأمريكي مقابل استدارتها في البريطاني"
        ),

        // DIPHTHONGS
        IpaSound(
            symbol = "/eɪ/",
            name = "Closing Diphthong (Face Vowel)",
            arabicName = "صوت مركب ينزلق من e إلى ɪ",
            soundType = "DIPHTHONG",
            usExampleWord = "Face, Day, Great",
            ukExampleWord = "Face, Day, Great",
            usPronunciationNotes = "Smooth glide from mid-front /e/ towards high-front /ɪ/.",
            ukPronunciationNotes = "RP starts slightly more open in some accents.",
            articulationGuideAr = "ابدأ بفتحة متوسطة ثم انزلق بسلاسة نحو الياء الخفيفة دون توقف.",
            minimalPairWordA = "Late /leɪt/",
            minimalPairWordB = "Let /let/",
            minimalPairDifference = "Late (متأخر) صوت مركب ممتد مقابل Let (يدع) صوت بسيط قصير"
        ),
        IpaSound(
            symbol = "/əʊ/ vs /oʊ/",
            name = "Goat Vowel (Key US vs UK Distinction)",
            arabicName = "صوت الواو المركب (الفرق بين الأمريكي والبريطاني)",
            soundType = "DIPHTHONG",
            usExampleWord = "Go, Boat, Home -> pronounced /ɡoʊ/, /boʊt/",
            ukExampleWord = "Go, Boat, Home -> pronounced /ɡəʊ/, /bəʊt/",
            usPronunciationNotes = "Starts back and rounded at [o] and glides to [ʊ].",
            ukPronunciationNotes = "Starts in the center at schwa [ə] and glides back to [ʊ].",
            articulationGuideAr = "في البريطاني تبدأ من صوت الشوا المحايد ثم تضم الشفتين، وفي الأمريكي تبدأ بضم واضح من البداية.",
            minimalPairWordA = "US: No /noʊ/",
            minimalPairWordB = "UK: No /nəʊ/",
            minimalPairDifference = "البداية المحايدة في البريطاني تجعل كلمة No تبدو كأنها 'نِـوْ' خفيفة"
        ),

        // CONSONANTS
        IpaSound(
            symbol = "/θ/",
            name = "Voiceless Dental Fricative",
            arabicName = "حرف الثاء العربي (صوت مهموس بدون اهتزاز أوتار)",
            soundType = "CONSONANT",
            usExampleWord = "Think, Three, Path",
            ukExampleWord = "Think, Three, Path",
            usPronunciationNotes = "Tongue tip placed between teeth or touching back of upper front teeth.",
            ukPronunciationNotes = "Identical articulation.",
            articulationGuideAr = "أخرج طرف لسانك بين أسنانك العليا والسفلى واطرد الهواء بدون تشغيل أوتارك الصوتية.",
            minimalPairWordA = "Think /θɪŋk/",
            minimalPairWordB = "Sink /sɪŋk/",
            minimalPairDifference = "طرف اللسان بين الأسنان في Think مقابل خلف الأسنان في Sink (حرف السين)"
        ),
        IpaSound(
            symbol = "/ð/",
            name = "Voiced Dental Fricative",
            arabicName = "حرف الذال العربي (صوت مجهور باهتزاز أوتار صوتية)",
            soundType = "CONSONANT",
            usExampleWord = "This, Mother, Breathe",
            ukExampleWord = "This, Mother, Breathe",
            usPronunciationNotes = "Tongue tip between teeth, vocal cords vibrating.",
            ukPronunciationNotes = "Identical articulation.",
            articulationGuideAr = "نفس مخرج الثاء مع وضع يدك على حنجرتك والشعور باهتزاز الأوتار الصوتية (كالذال).",
            minimalPairWordA = "Breathe /bri:ð/ (Verb)",
            minimalPairWordB = "Breath /breθ/ (Noun)",
            minimalPairDifference = "فعل يتنفس بالذال /ð/ واسم النفس بالثاء /θ/"
        ),
        IpaSound(
            symbol = "/t/ vs [ɾ] (Flap T)",
            name = "Alveolar Tap / Flap (US) vs True T (UK)",
            arabicName = "صوت التاء السريع الأمريكي (Flap T) مقابل التاء الواضحة البريطانية",
            soundType = "CONSONANT",
            usExampleWord = "Water, Better, City -> pronounced [ˈwɑːɾər], [ˈbeɾər]",
            ukExampleWord = "Water, Better, City -> pronounced [ˈwɔːtə] with true crisp /t/",
            usPronunciationNotes = "When /t/ falls between two vowels and before an unstressed syllable, it becomes a voiced tap [ɾ] sounding like a rapid 'd' or Arabic soft 'R'.",
            ukPronunciationNotes = "Pronounced as an aspirated crisp [tʰ] or glottal stop [ʔ].",
            articulationGuideAr = "في الأمريكي انقر طرف لسانك نقرة خاطفة على سقف الحلق كأنها راء عربية سريعة أو دال خفيفة.",
            minimalPairWordA = "US: Butter [ˈbʌɾər]",
            minimalPairWordB = "UK: Butter [ˈbʌtə]",
            minimalPairDifference = "التاء السريعة والراء في الأمريكي مقابل التاء الهوائية واختفاء الراء في البريطاني"
        )
    )

    private val initialSkillExercises = listOf(
        SkillExercise(
            skillType = "LISTENING",
            cefrLevel = "B1",
            title = "Podcast: Coffee Culture Across the Atlantic",
            arabicTitle = "بودكاست: ثقافة القهوة بين لندن ونيويورك",
            contentEn = "Host: Welcome to English Echoes! Today we are discussing ordering coffee. In New York, baristas appreciate speed and clarity. A customer might simply say: 'Can I get a large drip coffee with oat milk, to go?' Meanwhile, in London, politeness markers are abundant: 'Could I please have a flat white, and would you mind if I took a seat by the window?' Notice the tone and pace!",
            contentArSummary = "مقارنة ممتعة بين أسلوب طلب القهوة المباشر والسريع في نيويورك، والأسلوب المليء بعبارات اللباقة غير المباشرة في لندن.",
            questionsJson = "What do New York baristas prioritize?|||Elaborate greetings|||Speed and clarity|||Fancy cups|||1|||As mentioned, in NY baristas appreciate speed and directness.###Which coffee drink was specifically ordered in the London example?|||Espresso|||Flat White|||Cappuccino|||1|||The London customer ordered a Flat White.",
            isCompleted = false
        ),
        SkillExercise(
            skillType = "READING",
            cefrLevel = "B2",
            title = "Article: How Multilingual Minds Process Thought",
            arabicTitle = "مقال: كيف تُعالج العقول متعددة اللغات الأفكار",
            contentEn = "Does the language you speak influence the way you perceive the world? Cognitive scientists have long investigated the linguistic relativity principle. When bilingual speakers switch between Arabic and English, brain scans reveal heightened neural flexibility and enhanced problem-solving capabilities. Navigating two linguistic systems strengthens executive control and expands cultural empathy.",
            contentArSummary = "مقال علمي شيق يوضح كيف يعزز التحدث بلغتين (مثل العربية والإنجليزية) من مرونة الدماغ والقدرة على حل المشكلات المعقدة.",
            questionsJson = "What do brain scans reveal when bilinguals switch languages?|||Confusion and stress|||Heightened neural flexibility|||Memory loss|||1|||Scans show heightened neural flexibility and enhanced executive control.###What is the linguistic relativity principle concerned with?|||Typing speed|||Language influencing perception|||Phonetic pitch|||1|||It investigates how language influences perception of the world.",
            isCompleted = false
        ),
        SkillExercise(
            skillType = "SPEAKING",
            cefrLevel = "B1",
            title = "Roleplay: Tech Job Interview",
            arabicTitle = "محاكاة: مقابلة عمل وظيفية في مجال التقنية",
            contentEn = "Interviewer: 'Welcome! Can you describe a challenging technical problem you solved recently and what methodology you used?'\nCandidate Goal: Explain a situation, your task, action, and the result (STAR method).",
            contentArSummary = "تدريب على الإجابة عن أسئلة المقابلات الوظيفية باللغة الإنجليزية باستخدام أسلوب STAR.",
            questionsJson = "I encountered a high latency issue in our database. I optimized the queries and reduced response time by 40%.",
            isCompleted = false
        ),
        SkillExercise(
            skillType = "WRITING",
            cefrLevel = "B1",
            title = "Common Grammar Pitfalls for Arabic Speakers",
            arabicTitle = "تمارين تصحيح الأخطاء الشائعة للمتحدثين بالعربية",
            contentEn = "Arabic and English differ structurally. In Arabic, the 'to be' verb is omitted in present nominal sentences, leading many learners to mistakenly say: 'He teacher' instead of 'He is a teacher'. Also, prepositions require careful attention.",
            contentArSummary = "تدرب على تصحيح 3 جمل شائعة الخطأ يقع فيها معظم دارسي الإنجليزية.",
            questionsJson = "Fix this error: 'She is living in London since three years.'|||She has been living in London for three years.|||مع المدة الزمنية نستخدم for وليس since، ونستخدم المضارع التام المستمر للحدث المستمر.###Fix this error: 'I am agree with your opinion.'|||I agree with your opinion.|||agree هو فعل بحد ذاته ولا يحتاج إلى فعل الكينونة am.",
            isCompleted = false
        )
    )
}
