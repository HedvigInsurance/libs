package com.hedvig.libs.translations

import java.util.Locale

/**
 * A container of localized strings that can be retrieved by key and locale.
 */
interface Translations {
    fun get(key: String, locale: Locale): String?
}

