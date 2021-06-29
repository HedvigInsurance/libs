package com.hedvig.libs.translations

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class RemoteJsonFileTranslationsClient(
    private val url: String = "https://s3.eu-central-1.amazonaws.com/translations.hedvig.com/platform/translations.json",
    refreshRateMinutes: Long = 10
) : TranslationsClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper()
    private var translationsByLocale: JsonNode = objectMapper.readTree("{}")

    init {

        // Initial refresh, will throw error if fails
        logger.info("Doing initial refresh of translations")
        refreshTranslations(false)

        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                timerTask {
                    refreshTranslations(true)
                },
                refreshRateMinutes,
                refreshRateMinutes, TimeUnit.MINUTES
            )
    }

    private fun refreshTranslations(fallbackToExisting: Boolean) {

        try {
            val startTime = System.currentTimeMillis()
            val newTranslations = getRemoteFileAsJson(url)
            logger.info("Done refreshTranslations, took: ${System.currentTimeMillis() - startTime} ms")
            translationsByLocale = newTranslations
        } catch (e: Exception) {
            // Keep on using the existing one
            logger.error("Failed to download translations json file.", e)
            if (!fallbackToExisting) {
                throw e
            }
        }
    }

    private fun getRemoteFileAsJson(url: String): JsonNode {
        URL(url).openStream().use { input ->
            return objectMapper.readTree(input)
        }
    }

    override fun getTranslation(locale: Locale, key: String): String? {
        return translationsByLocale[locale.toString()]?.get(key)?.asText()
    }
}
