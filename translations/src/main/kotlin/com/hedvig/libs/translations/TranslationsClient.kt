package com.hedvig.libs.translations

import java.util.Locale

interface TranslationsClient {
    fun getTranslation(locale: Locale, key: String): String?
}

