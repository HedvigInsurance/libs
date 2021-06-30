package com.hedvig.libs.translations

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.util.Locale

class TranslationsTest {

    @Test
    @Disabled
    // This test can be enabled/run locally when developing, but we don't won't to rely on S3 or specific translation in CI
    fun test() {
        val client =
            RemoteJsonFileTranslations(
                "https://s3.eu-central-1.amazonaws.com/translations.hedvig.com/platform/translations.json",
                1
            )

        var translation = client.get("DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE", Locale("da", "DK"))
        assertThat(translation).isEqualTo("Størrelse")

        translation = client.get("DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE", Locale("en", "DK"))
        assertThat(translation).isEqualTo("Size")

        // Unsupported text key
        translation = client.get("X_WHATEVER_X", Locale("da", "DK"))
        assertThat(translation).isNull()

        // Unsupported locale
        translation = client.get("DK_CONTENT_CONVERSATION_SIZE_TOOLTIP_TITLE", Locale("sv", "DK"))
        assertThat(translation).isNull()
    }

    @Test
    fun testFailToInit() {

        assertThrows<IOException>("Should fail to init") {
            RemoteJsonFileTranslations("https://234567lkjhgfdzxcvbnj8765.com")
        }
    }
}
