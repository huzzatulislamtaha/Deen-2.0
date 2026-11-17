package com.example.data.content

import com.example.domain.model.QuranExcerpt

/**
 * Verified essential daily Quran Surahs and Ayahs (Uthmani/Standard Unicode Arabic
 * with Harakat, Sahih International English translation, and faithful Bengali meaning).
 */
object VerifiedQuranContent {
    val excerpts: List<QuranExcerpt> = listOf(
        QuranExcerpt(
            id = "quran_fatihah",
            surahNumber = 1,
            ayahRange = "1:1–7",
            titleEn = "Surah Al-Fatihah (The Opening)",
            titleBn = "সূরা আল-ফাতিহা (সূচনা)",
            titleAr = "سُورَةُ الْفَاتِحَةِ",
            arabicText = """
                بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ ۝١
                ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ ۝٢
                ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ ۝٣
                مَـٰلِكِ يَوْمِ ٱلدِّينِ ۝٤
                إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝٥
                ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ ۝٦
                صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ ۝٧
            """.trimIndent(),
            bengaliTranslation = """
                ১. পরম করুণাময়, অসীম দয়ালু আল্লাহর নামে।
                ২. সমস্ত প্রশংসা জগৎসমূহের প্রতিপালক আল্লাহর জন্য।
                ৩. তিনি পরম করুণাময়, অসীম দয়ালু।
                ৪. বিচার দিনের মালিক।
                ৫. আমরা একমাত্র আপনারই ইবাদত করি এবং একমাত্র আপনারই নিকট সাহায্য প্রার্থনা করি।
                ৬. আমাদেরকে সরল পথ প্রদর্শন করুন।
                ৭. সে সকল লোকের পথ, যাদের প্রতি আপনি অনুগ্রহ করেছেন; তাদের পথ নয়, যাদের প্রতি আপনার গজব নাযিল হয়েছে এবং যারা পথভ্রষ্ট হয়েছে।
            """.trimIndent(),
            englishTranslation = """
                1. In the name of Allah, the Entirely Merciful, the Especially Merciful.
                2. [All] praise is [due] to Allah, Lord of the worlds —
                3. The Entirely Merciful, the Especially Merciful,
                4. Sovereign of the Day of Recompense.
                5. It is You we worship and You we ask for help.
                6. Guide us to the straight path —
                7. The path of those upon whom You have bestowed favor, not of those who have earned [Your] anger or of those who are astray.
            """.trimIndent(),
            sourceNote = "Surah Al-Fatihah (Chapter 1) • Sahih International",
            sourceUrl = "https://quran.com/1"
        ),
        QuranExcerpt(
            id = "quran_ayat_kursi",
            surahNumber = 2,
            ayahRange = "2:255",
            titleEn = "Ayat al-Kursi (The Verse of the Throne)",
            titleBn = "আয়াতুল কুরসী (সূরা আল-বাকারাহ: ২৫৫)",
            titleAr = "آيَةُ الْكُرْسِيِّ",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            bengaliTranslation = "আল্লাহ—তিনি ছাড়া কোনো সত্য ইলাহ নেই; তিনি চিরঞ্জীব, সর্বসত্তার ধারক। তাঁকে তন্দ্রা ও নিদ্রা স্পর্শ করে না। আসমানসমূহ ও জমিনে যা কিছু আছে সবকিছু তাঁরই। কে আছে যে তাঁর অনুমতি ব্যতীত তাঁর নিকট সুপারিশ করবে? তাদের সম্মুখে ও পশ্চাতে যা আছে তা তিনি জানেন। আর তারা তাঁর জ্ঞানের কোনো কিছুই আয়ত্ত করতে পারে না, তবে তিনি যা ইচ্ছা করেন তা ব্যতীত। তাঁর কুরসী আসমানসমূহ ও জমিন পরিব্যাপ্ত করে আছে এবং এতদুভয়ের রক্ষণাবেক্ষণ তাঁকে ক্লান্ত করে না। আর তিনি সুউচ্চ, মহামহিম।",
            englishTranslation = "Allah — there is no deity except Him, the Ever-Living, the Self-Sustaining. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
            sourceNote = "Surah Al-Baqarah (2:255) • Sahih International",
            sourceUrl = "https://quran.com/2/255"
        ),
        QuranExcerpt(
            id = "quran_ikhlas",
            surahNumber = 112,
            ayahRange = "112:1–4",
            titleEn = "Surah Al-Ikhlas (Sincerity)",
            titleBn = "সূরা আল-ইখলাস (একনিষ্ঠতা)",
            titleAr = "سُورَةُ الْإِخْلَاصِ",
            arabicText = """
                بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ
                قُلْ هُوَ اللَّهُ أَحَدٌ ۝١
                اللَّهُ الصَّمَدُ ۝٢
                لَمْ يَلِدْ وَلَمْ يُولَدْ ۝٣
                وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ۝٤
            """.trimIndent(),
            bengaliTranslation = """
                ১. বলুন, তিনি আল্লাহ, একক অদ্বিতীয়।
                ২. আল্লাহ অমুখাপেক্ষী (সকলে তাঁর মুখাপেক্ষী)।
                ৩. তিনি কাউকে জন্ম দেননি এবং তাঁকেও জন্ম দেওয়া হয়নি।
                ৪. আর তাঁর সমতুল্য কেউ নেই।
            """.trimIndent(),
            englishTranslation = """
                1. Say, "He is Allah, [who is] One,
                2. Allah, the Eternal Refuge.
                3. He neither begets nor is born,
                4. Nor is there to Him any equivalent."
            """.trimIndent(),
            sourceNote = "Surah Al-Ikhlas (Chapter 112) • Sahih International",
            sourceUrl = "https://quran.com/112"
        ),
        QuranExcerpt(
            id = "quran_falaq",
            surahNumber = 113,
            ayahRange = "113:1–5",
            titleEn = "Surah Al-Falaq (The Daybreak)",
            titleBn = "সূরা আল-ফালাক (উষা)",
            titleAr = "سُورَةُ الْفَلَقِ",
            arabicText = """
                بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ
                قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝١
                مِن شَرِّ مَا خَلَقَ ۝٢
                وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝٣
                وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝٤
                وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ۝٥
            """.trimIndent(),
            bengaliTranslation = """
                ১. বলুন, আমি আশ্রয় প্রার্থনা করছি ঊষার প্রতিপালকের,
                ২. তিনি যা সৃষ্টি করেছেন তার অনিষ্ট হতে,
                ৩. অন্ধকার রাতের অনিষ্ট হতে যখন তা গভীর হয়,
                ৪. গ্রন্থিসমূহে ফুঁকদানকারী জাদুকরদের অনিষ্ট হতে,
                ৫. এবং হিংসুকের অনিষ্ট হতে যখন সে হিংসা করে।
            """.trimIndent(),
            englishTranslation = """
                1. Say, "I seek refuge in the Lord of daybreak
                2. From the evil of that which He created
                3. And from the evil of darkness when it settles
                4. And from the evil of the blowers in knots
                5. And from the evil of an envier when he envies."
            """.trimIndent(),
            sourceNote = "Surah Al-Falaq (Chapter 113) • Sahih International",
            sourceUrl = "https://quran.com/113"
        ),
        QuranExcerpt(
            id = "quran_nas",
            surahNumber = 114,
            ayahRange = "114:1–6",
            titleEn = "Surah An-Nas (Mankind)",
            titleBn = "সূরা আন-নাস (মানবজাতি)",
            titleAr = "سُورَةُ النَّاسِ",
            arabicText = """
                بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ
                قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝١
                مَلِكِ النَّاسِ ۝٢
                إِلَٰهِ النَّاسِ ۝٣
                مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝٤
                الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝٥
                مِنَ الْجِنَّةِ وَالنَّاسِ ۝٦
            """.trimIndent(),
            bengaliTranslation = """
                ১. বলুন, আমি আশ্রয় প্রার্থনা করছি মানুষের প্রতিপালকের,
                ২. মানুষের অধিপতির,
                ৩. মানুষের ইলাহের (উপাস্যের),
                ৪. আত্মগোপনকারী কুমন্ত্রণাদাতার অনিষ্ট হতে,
                ৫. যে মানুষের অন্তরে কুমন্ত্রণা দেয়,
                ৬. জিন ও মানুষের মধ্য থেকে।
            """.trimIndent(),
            englishTranslation = """
                1. Say, "I seek refuge in the Lord of mankind,
                2. The Sovereign of mankind,
                3. The God of mankind,
                4. From the evil of the retreating whisperer —
                5. Who whispers [evil] into the breasts of mankind —
                6. From among the jinn and mankind."
            """.trimIndent(),
            sourceNote = "Surah An-Nas (Chapter 114) • Sahih International",
            sourceUrl = "https://quran.com/114"
        )
    )
}
