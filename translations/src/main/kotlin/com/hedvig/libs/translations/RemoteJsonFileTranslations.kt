package com.hedvig.libs.translations

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * An implementation of [Translations] that is backed by a JSON file fetched from an S3 bucket.
 *
 * This file can be configured, but the default is `s3://translations.hedvig.com/platform/translations.json`.
 */
class RemoteJsonFileTranslations(
    private val url: String = "https://s3.eu-central-1.amazonaws.com/translations.hedvig.com/platform/translations.json",
    refreshRateMinutes: Long = 10
) : Translations {

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

    override fun get(key: String, locale: Locale): String? {
        val translation = translationsByLocale[locale.toString()]?.get(key)?.asText()
        if (translation.isNullOrEmpty()) {
            logger.warn("Missing translation for requested key: $key")
        }
        return translation
    }
}
