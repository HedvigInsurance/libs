package com.hedvig.libs.logging.mdc

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Service
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [MdcScopeAspect::class],
    properties = ["hedvig.logging.mdc.prefix=test"]
)
@ComponentScan("com.hedvig.libs.logging.mdc")
@EnableAspectJAutoProxy
class MdcScopeAspectTest {

    @Autowired
    private lateinit var worker: SomeWorker

    @BeforeEach
    fun setup() {
        MDC.remove("test.taskId")
    }

    @Test
    fun `test mdc is set`() {
        val uuid = UUID.randomUUID()
        worker.withContext(taskId = uuid) {
            assertThat(MDC.get("test.taskId")).isEqualTo(uuid.toString())
        }
    }

    @Test
    fun `test mdc is unset afterwards`() {
        val uuid = UUID.randomUUID()
        worker.withContext(taskId = uuid) {
        }
        assertThat(MDC.get("test.taskId")).isNull()
    }

    @Test
    fun `test mdc is restored afterwards`() {
        MDC.put("test.taskId", "previous value")
        val uuid = UUID.randomUUID()
        worker.withContext(taskId = uuid) {
            assertThat(MDC.get("test.taskId")).isEqualTo(uuid.toString())
        }
        assertThat(MDC.get("test.taskId")).isEqualTo("previous value")
    }

    @Test
    fun `test mdc is restored despite exception being thrown`() {
        val uuid = UUID.randomUUID()
        assertThrows<RuntimeException> {
            worker.withContext(taskId = uuid) {
                throw RuntimeException("Exception")
            }
        }
        assertThat(MDC.get("test.taskId")).isNull()
    }

    @Service
    internal class SomeWorker {
        @MdcScope
        fun withContext(@Mdc taskId: UUID, test: () -> Unit) {
            test()
        }
    }
}
