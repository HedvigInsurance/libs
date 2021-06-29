package com.hedvig.libs.translations

import java.util.Locale

interface TranslationsClient {
    fun getTranslation(key: String, locale: Locale): String?
}

