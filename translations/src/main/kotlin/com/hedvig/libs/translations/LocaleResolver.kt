package com.hedvig.libs.translations

import org.slf4j.LoggerFactory
import java.util.*

object LocaleResolver {

    fun resolveLocale(acceptLanguage: String?): Locale =
        resolveLocale(acceptLanguage, FALLBACK_LOCALE)!!

    fun resolveNullableLocale(acceptLanguage: String?): Locale? =
        resolveLocale(acceptLanguage, null)

    private fun resolveLocale(acceptLanguage: String?, defaultLocale: Locale?): Locale? {
        if (acceptLanguage.isNullOrBlank()) {
            return defaultLocale
        }

        return try {
            val list = Locale.LanguageRange.parse(acceptLanguage)
            Locale.lookup(list, LOCALES) ?: FALLBACK_LOCALE
        } catch (e: IllegalArgumentException) {
            loggger.error("IllegalArgumentException when parsing acceptLanguage: '$acceptLanguage' message: ${e.message}")
            defaultLocale
        }
    }

    private val LOCALES = listOf(
        Locale("en", "se"),
        Locale("sv", "se"),
        Locale("nb", "no"),
        Locale("en", "no"),
        Locale("da", "dk"),
        Locale("en", "dk")
    )

    val FALLBACK_LOCALE = Locale("sv", "se")
    private val loggger = LoggerFactory.getLogger(this::class.java)
}
