package com.hedvig.libs.logging.calls

import ch.qos.logback.classic.Logger
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import ch.qos.logback.classic.spi.ILoggingEvent

import ch.qos.logback.core.read.ListAppender
import com.hedvig.libs.logging.masking.Masked
import com.hedvig.libs.logging.mdc.Mdc
import com.hedvig.libs.logging.mdc.MdcScope
import com.hedvig.libs.logging.mdc.MdcScopeAspect
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

@RunWith(SpringRunner::class)
@SpringBootTest(classes= [LogCallAspect::class, MdcScopeAspect::class])
@ComponentScan("com.hedvig.libs.logging.calls")
@EnableAspectJAutoProxy
class LogCallTest {

    @TestConfiguration
    internal class TestServiceImplTestContextConfiguration {
        @Bean
        fun testService(): TestServiceX {
            return TestServiceX()
        }
    }

    @Autowired
    lateinit var testService: TestServiceX

    var logWatcher: ListAppender<ILoggingEvent>? = null

    @Before
    fun setup() {
        logWatcher = ListAppender()
        logWatcher!!.start()
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).addAppender(logWatcher)
    }

    @After
    fun cleanup() {
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).detachAppender(logWatcher)
    }

    @Test
    fun testNoLogging() {

        testService.logNothing("asd")

        assertThat(logWatcher!!.list).isEmpty()
    }

    @Test
    fun testNoParamsReturningUnit() {

        testService.logCallNoParamReturningUnit()

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].level.toString()).isEqualTo("INFO")
            assertThat(list[0].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[0].message).isEqualTo("Executing TestServiceX.logCallNoParamReturningUnit(), parameters: []")

            assertThat(list[1].level.toString()).isEqualTo("INFO")
            assertThat(list[1].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[1].message).startsWith("Executed: TestServiceX.logCallNoParamReturningUnit(), returned: '-'")
        }
    }

    @Test
    fun testParamsReturningString() {

        testService.logCallWithParamsReturningString("abc", 10)

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].level.toString()).isEqualTo("INFO")
            assertThat(list[0].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[0].message).isEqualTo("Executing TestServiceX.logCallWithParamsReturningString(..), parameters: [abc, 10]")

            assertThat(list[1].level.toString()).isEqualTo("INFO")
            assertThat(list[1].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[1].message).startsWith("Executed: TestServiceX.logCallWithParamsReturningString(..), returned: 'APA'")
        }
    }

    @Test
    fun testParamsReturningNullString() {

        testService.logCallWithParamsReturningNullString("abc", 10)

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].level.toString()).isEqualTo("INFO")
            assertThat(list[0].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[0].message).isEqualTo("Executing TestServiceX.logCallWithParamsReturningNullString(..), parameters: [abc, 10]")

            assertThat(list[1].level.toString()).isEqualTo("INFO")
            assertThat(list[1].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[1].message).startsWith("Executed: TestServiceX.logCallWithParamsReturningNullString(..), returned: 'null'")
        }
    }

    @Test
    fun testMaskedPojoParamsReturningPojo() {

        val pojoB = PojoB("11", "22", null)
        val pojoA = PojoA("1234", 1234, listOf("1", "2"), listOf(pojoB, pojoB), mapOf("1" to pojoB, "2" to pojoB))

        testService.logCallWithPojoParamsReturningPojo(pojoA)

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].level.toString()).isEqualTo("INFO")
            assertThat(list[0].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[0].message).isEqualTo("Executing TestServiceX.logCallWithPojoParamsReturningPojo(..), parameters: [PojoA(a=1234, b=***, c=[1, 2], d=[PojoB(a=11, b=***, d=null), PojoB(a=11, b=***, d=null)], e={1=PojoB(a=11, b=***, d=null), 2=PojoB(a=11, b=***, d=null)})]")

            assertThat(list[1].level.toString()).isEqualTo("INFO")
            assertThat(list[1].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[1].message).startsWith("Executed: TestServiceX.logCallWithPojoParamsReturningPojo(..), returned: 'PojoB(a=APA, b=***, d=[PojoB(a=ABC, b=***, d=null)])'")
        }
    }

    @Test
    fun testThrowingException() {

        assertThrows<IllegalArgumentException>("Testing") {
           testService.logCallThrowingException()
        }

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].level.toString()).isEqualTo("INFO")
            assertThat(list[0].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[0].message).isEqualTo("Executing TestServiceX.logCallThrowingException(), parameters: []")

            assertThat(list[1].level.toString()).isEqualTo("INFO")
            assertThat(list[1].loggerName).isEqualTo(TestServiceX::class.java.name + "-aop")
            assertThat(list[1].message).startsWith("Exception during executing TestServiceX.logCallThrowingException(): java.lang.IllegalArgumentException: Testing")
        }
    }

    @Test
    fun testMdcAnnoationsAreIncluded() {

        testService.logIncludingMdc(context = "abc")

        with(logWatcher!!) {
            assertThat(list.size).isEqualTo(2)
            assertThat(list[0].mdcPropertyMap).containsEntry("hedvig.context", "abc")

            assertThat(list[1].mdcPropertyMap).containsEntry("hedvig.context", "abc")
        }
    }
}


@Service
class TestServiceX {

    fun logNothing(param: String): String {
        return "APA"
    }

    @LogCall
    fun logCallNoParamReturningUnit() {
    }

    @LogCall
    fun logCallWithParamsReturningString(param1: String, param2: Int): String {
        return "APA"
    }

    @LogCall
    fun logCallWithParamsReturningNullString(param1: String, param2: Int): String? {
        return null
    }

    @LogCall
    fun logCallWithPojoParamsReturningPojo(param1: PojoA): PojoB? {
        return PojoB("APA", "BANAN", listOf(PojoB("ABC", "DEF", null)))
    }

    @LogCall
    fun logCallThrowingException(): String? {
        throw IllegalArgumentException("Testing")
    }

    @MdcScope
    @LogCall
    fun logIncludingMdc(@Mdc context: String) {
    }
}

data class PojoA (
    val a: String?,
    @Masked val b: Int?,
    val c: List<String>?,
    val d: List<PojoB>?,
    val e: Map<String, PojoB>?
)

data class PojoB (
    val a: String?,
    @Masked val b: String?,
    val d: List<PojoB>?
)
