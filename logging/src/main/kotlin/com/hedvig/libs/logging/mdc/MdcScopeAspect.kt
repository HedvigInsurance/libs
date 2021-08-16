package com.hedvig.libs.logging.mdc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * The aspect that actually extracts MDC values using the [Mdc] and [MdcScope] annotations.
 *
 * MDC values are logged with the given prefix, which can either be configured by creating a
 * custom Bean of this class, or setting the `hedvig.logging.mdc.prefix` property.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcScopeAspect(
    @Value("\${hedvig.logging.mdc.prefix:hedvig}")
    private val prefix: String
) {

    @Around("@annotation(com.hedvig.libs.logging.mdc.MdcScope)")
    fun applyMdc(joinPoint: ProceedingJoinPoint): Any? {
        val newContext = joinPoint.extractMdcProperties().mapKeys {
            "$prefix.${it.key}"
        }
        val stack = MdcStack.push(newContext)
        try {
            return joinPoint.proceed()
        } finally {
            stack.restore()
        }
    }
}