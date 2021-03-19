package com.hedvig.libs.logging.mdc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*
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
        extractMdcPropertiesRecursively(arg, parameter, mdc)
    }
    return mdc
}

private fun extractMdcPropertiesRecursively(
    value: Any,
    element: KAnnotatedElement,
    result: MutableMap<String, String>
) {
    // Don't try to do anything to lambdas
    if (value is Function<*>) return

    val mdcTag = element.findAnnotation<Mdc>()
    when {
        // String, Int and UUIDs are supported @Mdc types
        value is String || value is Int || value is UUID -> {
            if (mdcTag != null) {
                val propertyName = when {
                    mdcTag.name.isNotEmpty() -> mdcTag.name
                    element is KParameter -> element.name ?: throw IllegalArgumentException("Parameter has no name: $element")
                    element is KCallable<*> -> element.name
                    else -> throw IllegalArgumentException("Unsupported element type: $element")
                }
                result[propertyName] = value.toString()
            }
        }
        value.javaClass.packageName.startsWith("com.hedvig") -> {
            if (mdcTag != null) {
                throw IllegalArgumentException("@Mdc can only target String, Int or UUID")
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

            for ((parameter, property) in paramPropertyMatch) {
                val nestedValue = property.get(value) ?: continue
                extractMdcPropertiesRecursively(
                    nestedValue,
                    parameter,
                    result
                )
            }
        }
    }
}
