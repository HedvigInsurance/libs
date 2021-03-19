package com.hedvig.libs.logging.mdc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcScopeAspect {

    @Around("@annotation(com.hedvig.libs.logging.mdc.MdcScope)")
    fun applyMdc(joinPoint: ProceedingJoinPoint): Any? {
        val newContext = joinPoint.extractMdcProperties()
        val existing = pushMdc(newContext)
        val result = joinPoint.proceed()
        restoreMdc(newContext, existing)
        return result
    }

    private fun pushMdc(newContext: Map<String, String>): Map<String, String> {
        val existing = mutableMapOf<String, String>()
        newContext.forEach { (key, value) ->
            MDC.get(key)?.let { existing[key] = it }
            MDC.put(key, value)
        }
        return existing
    }

    private fun restoreMdc(contextToDelete: Map<String, String>, contextToRestore: Map<String, String>) {
        contextToDelete.forEach { (key, value) ->
            contextToRestore[key]?.let { restored ->
                MDC.put(key, restored)
            } ?: run {
                MDC.remove(key)
            }
        }
    }
}