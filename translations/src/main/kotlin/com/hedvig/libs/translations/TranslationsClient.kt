package com.hedvig.libs.translations

interface TranslationsClient {
    fun getTranslation(locale: String, key: String): String?
}

