package com.hedvig.libs.logging.mdc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

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
        extractMdcPropertiesRecursively(arg, parameter, mdc)
    }
    return mdc
}

private fun extractMdcPropertiesRecursively(
    value: Any,
    parameter: KParameter,
    result: MutableMap<String, String>
) {
    // Don't try to do anything to lambdas
    if (value is Function<*>) return

    val mdcTag = parameter.findAnnotation<Mdc>()
    when {
        // String, Int and UUIDs are supported @Mdc types
        value is String || value is Number || value is UUID -> {
            if (mdcTag != null) {
                val propertyName = when {
                    mdcTag.name.isNotEmpty() -> mdcTag.name
                    else -> parameter.name ?: throw IllegalArgumentException("Parameter has no name: $parameter")
                }
                result[propertyName] = value.toString()
            }
        }
        value.javaClass.packageName.startsWith("com.hedvig") -> {
            if (mdcTag != null) {
                throw IllegalArgumentException("@Mdc can only target String, Number or UUID")
            }

            @Suppress("UNCHECKED_CAST") // Needed to make compiler happy
            val clazz = value::class as KClass<Any>

            // In Kotlin, if you annotate properties as part of the primary constructor
            // the annotations end up on the constructor parameters, not on the actual
            // field or property. Therefore, we must dig them out like this.
            // Example:
            // data class Request(
            //   @Mdc val taskId: UUID   <-- this is annotated on the constructor param, not the field or property
            // )
            // See: https://kotlinlang.org/docs/annotations.html#annotation-use-site-targets
            val paramPropertyMatch = clazz.primaryConstructor!!.parameters.mapNotNull { param ->
                val property = clazz.memberProperties.find { it.name == param.name } ?: return@mapNotNull null
                param to property
            }

            for ((param, property) in paramPropertyMatch) {
                val nestedValue = property.get(value) ?: continue
                extractMdcPropertiesRecursively(
                    nestedValue,
                    param,
                    result
                )
            }
        }
    }
}
