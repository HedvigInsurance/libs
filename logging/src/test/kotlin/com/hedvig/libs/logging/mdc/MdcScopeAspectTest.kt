package com.hedvig.libs.logging.mdc

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Service
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [MdcScopeAspect::class])
@ComponentScan("com.hedvig.libs.logging.mdc")
@EnableAspectJAutoProxy
class MdcScopeAspectTest {

    @Autowired
    private lateinit var worker: SomeWorker

    @Test
    fun `test mdc propagates`() {
        val uuid = UUID.randomUUID()
        worker.doWork(uuid)
    }

    @Service
    internal class SomeWorker {
        @MdcScope
        fun doWork(@Mdc taskId: UUID) {
            assertThat(MDC.get("taskId")).isEqualTo(taskId.toString())
        }
    }
}
