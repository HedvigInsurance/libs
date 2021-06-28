package com.hedvig.libs.translations

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Timer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class RemoteJsonFileClient(
    private val url: String = "https://s3.eu-central-1.amazonaws.com/translations.hedvig.com/platform/all_locales.json",
    refreshRateMinutes: Long = 10
) : TranslationsClient {

    private val logger = LoggerFactory.getLogger(RemoteJsonFileClient::class.java)
    private val objectMapper = ObjectMapper()
    private var translationsByLocale: JsonNode = objectMapper.readTree("{}")
    private val initialRefreshFuture: Future<Unit> =
        CompletableFuture.supplyAsync { translationsByLocale = refreshTranslations() }

    init {
        val refreshRateMs = TimeUnit.MINUTES.toMillis(refreshRateMinutes)

        Timer().schedule(timerTask {
            translationsByLocale = refreshTranslations()
        }, refreshRateMs, refreshRateMs)
    }

    private fun refreshTranslations(): JsonNode {

        try {
            val startTime = System.currentTimeMillis()
            val newTranslations = getRemoteFileAsJson(url)
            logger.debug("Done refreshTranslations, took: ${System.currentTimeMillis() - startTime} ms")
            return newTranslations
        } catch (e: Exception) {
            // Return the existing one
            logger.error("Failed to download translations json file: ${e.message}", e)
            return translationsByLocale
        }
    }

    private fun getRemoteFileAsJson(url: String): JsonNode {
        URL(url).openStream().use { input ->
            return objectMapper.readTree(input)
        }
    }

    private fun waitForInitialRefresh() {
        if (!initialRefreshFuture.isDone) {
            logger.debug("Initial refresh not yet done, will wait for it.")
            initialRefreshFuture.get()
            logger.debug("Initial refresh done.")
        }
    }

    override fun getTranslation(locale: String, key: String): String? {

        // Ensure the initial fetch is done
        waitForInitialRefresh()

        return if (translationsByLocale[locale] != null) {
            translationsByLocale[locale].get(key)?.asText("")!!
        } else {
            null
        }
    }
}
