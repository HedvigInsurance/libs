package com.hedvig.libs.logging.web

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import org.jboss.logging.MDC
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class HedvigMdcFilterTest {

    @Autowired
    lateinit var template: TestRestTemplate

    @Autowired
    lateinit var controller: Controller

    @AfterEach
    fun tearDown(@Autowired controller: Controller) {
        controller.mdcValues.clear()
    }

    @Test
    fun `test 0 context values`() {
        val request = RequestEntity.get(URI("/test/mdc"))
            .build()
        assertThat {
            template.exchange<Any>(request).statusCode
        }.isSuccess().isEqualTo(HttpStatus.OK)

        assertThat {
            controller.mdcValues
        }.isSuccess().isEmpty()
    }

    @Test
    fun `test 1 context value`() {
        val request = RequestEntity.get(URI("/test/mdc"))
            .header("Hedvig.token", "12345")
            .build()
        assertThat {
            template.exchange<Any>(request).statusCode
        }.isSuccess().isEqualTo(HttpStatus.OK)

        assertThat {
            controller.mdcValues
        }.isSuccess().contains("hedvig.memberId" to "12345")
    }

    @Test
    fun `test 2 context values`() {
        val request = RequestEntity.get(URI("/test/mdc"))
            .header("Hedvig.token", "12345")
            .header("Accept-Language", "sv_SE")
            .build()
        assertThat {
            template.exchange<Any>(request).statusCode
        }.isSuccess().isEqualTo(HttpStatus.OK)

        assertThat {
            controller.mdcValues
        }.isSuccess().containsAll(
            "hedvig.memberId" to "12345",
            "hedvig.locale" to "sv_SE",
        )
    }

    @Test
    fun `test context is cleared after request`() {
        val request1 = RequestEntity.get(URI("/test/mdc"))
            .header("Hedvig.token", "12345")
            .build()
        assertThat {
            template.exchange<Any>(request1).statusCode
        }.isSuccess().isEqualTo(HttpStatus.OK)
        controller.mdcValues.clear()

        // Fire a bunch of requests to hit the same thread again, making sure we get the same
        // thead-local MDC
        (0..20).forEach { _ ->
            val request2 = RequestEntity.get(URI("/test/mdc"))
                .build()
            assertThat {
                template.exchange<Any>(request2).statusCode
            }.isSuccess().isEqualTo(HttpStatus.OK)
        }

        assertThat {
            controller.mdcValues
        }.isSuccess().isEmpty()
    }
}

@Configuration
class Config {
    @Bean
    fun filter() = HedvigMdcFilter()
}

@RestController
class Controller {

    val mdcValues = mutableSetOf<Pair<String, Any>>()

    @GetMapping("/test/mdc")
    fun mdcTest() {
        MDC.getMap().forEach { (key, value) ->
            mdcValues.add(key to value)
        }
    }
}
