package com.hedvig.libs.logging.mdc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mdc(
    val name: String = ""
)

internal fun ProceedingJoinPoint.extractMdcProperties(): Map<String, String> {
    val signature = (signature as? MethodSignature) ?: return emptyMap()
    val method = signature.method.kotlinFunction ?: return emptyMap()
    return extractMdcProperties(method, args)
}

internal fun extractMdcProperties(method: KFunction<*>, args: Array<Any>): Map<String, String> {
    val mdc = mutableMapOf<String, String>()
    args.indices.forEach { index ->
        val arg = args[index]
        val parameter = method.valueParameters[index]
        parameter.annotations.filterIsInstance<Mdc>().firstOrNull()?.let { annotation ->
            val property = if (annotation.name.isNotBlank()) annotation.name else parameter.name ?: "arg$index"
            mdc[property] = arg.toString()
        }
    }
    return mdc
}
