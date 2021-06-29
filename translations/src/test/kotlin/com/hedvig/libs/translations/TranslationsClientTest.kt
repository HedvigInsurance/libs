package com.hedvig.libs.translations

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.util.Locale

class TranslationsClientTest {

    @Test
    @Disabled
    // This test can be enabled/run locally when developing, but we don't won't to rely on S3 or specific translation in CI
    fun test() {
        val client =
            RemoteJsonFileTranslationsClient(
                "https://s3.eu-central-1.amazonaws.com/translations.hedvig.com/platform/translations.json",
                1
            )

        var translation = client.getTranslation(Locale("da", "DK"), "DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE")
        assertThat(translation).isEqualTo("Størrelse")

        translation = client.getTranslation(Locale("en", "DK"), "DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE")
        assertThat(translation).isEqualTo("Størrelse")

        // Unsupported text key
        translation = client.getTranslation(Locale("da", "DK"), "X_WHATEVER_X")
        assertThat(translation).isNull()

        // Unsupported locale
        translation = client.getTranslation(Locale("sv", "DK"), "DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE")
        assertThat(translation).isNull()
    }

    @Test
    fun testFailToInit() {

        assertThrows<IOException>("Should fail to init") {
            RemoteJsonFileTranslationsClient("https://234567lkjhgfdzxcvbnj8765.com")
        }
    }
}
