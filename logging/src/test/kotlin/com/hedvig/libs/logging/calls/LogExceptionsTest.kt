package com.hedvig.libs.logging.calls

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.hedvig.libs.logging.mdc.MdcScopeAspect
import com.hedvig.logging.calls.LogExceptions
import com.hedvig.logging.calls.LogExceptionsAspect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LogExceptionsAspect::class])
@ComponentScan("com.hedvig.libs.logging.calls")
@EnableAspectJAutoProxy
class LogExceptionsTest {

    @Autowired
    lateinit var handler: ExceptionHandler

    private lateinit var logWatcher: ListAppender<ILoggingEvent>

    @BeforeEach
    fun setup() {
        logWatcher = ListAppender()
        logWatcher.start()
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).addAppender(logWatcher)
    }

    @AfterEach
    fun cleanup() {
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).detachAppender(logWatcher)
    }

    @Test
    fun testNoLogging() {

        handler.logNothing("asd")

        assertThat(logWatcher.list).isEmpty()
    }

    @Test
    fun testNoParamsReturningUnit() {

        handler.logException(RuntimeException("Test exception"))
        with(logWatcher) {
            assertThat(list.size).isEqualTo(1)
            assertThat(list[0].level.toString()).isEqualTo("WARN")
            assertThat(list[0].message).isEqualTo("Test exception")
            assertThat(list[0].throwableProxy.stackTraceElementProxyArray.size).isGreaterThan(0)
        }
    }
}

@Component
@LogExceptions
class ExceptionHandler {

    fun logNothing(param: String): String {
        return "APA"
    }

    fun logException(e: Exception): Exception {
        return e
    }
}
