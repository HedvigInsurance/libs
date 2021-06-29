package com.hedvig.libs.translations

import org.slf4j.LoggerFactory
import java.util.Locale

object LocaleResolver {

    fun resolve(acceptLanguage: String?): Locale =
        resolve(acceptLanguage, FALLBACK_LOCALE)!!

    fun resolveNullable(acceptLanguage: String?): Locale? =
        resolve(acceptLanguage, null)

    private fun resolve(acceptLanguage: String?, defaultLocale: Locale?): Locale? {
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
