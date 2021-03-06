package com.hedvig.libs.logging.masking

import java.lang.reflect.Modifier
import java.util.LinkedList

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Masked

fun Any?.toMaskedString(): String = when (this) {
    is Collection<*> -> this.toMaskedString()
    is Map<*, *> -> this.toMaskedString()
    null -> "null"
    else -> reflectionToString(this)
}

fun Collection<*>?.toMaskedString(): String =
    this?.joinToString(", ", "[", "]") { it.toMaskedString() } ?: "null"

fun Map<*, *>?.toMaskedString(): String =
    this?.map { (key, value) -> "$key=${value.toMaskedString()}" }?.joinToString(", ", "{", "}") ?: "null"

private fun reflectionToString(obj: Any): String {

    if (!isWhitelisted(obj.javaClass))
        return obj.toString()

    if (obj.javaClass.isEnum())
        return obj.toString()

    val s = LinkedList<String>()
    var clazz: Class<in Any>? = obj.javaClass

    while (clazz != null && isWhitelisted(clazz)) {

        for (prop in clazz.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }) {

            prop.isAccessible = true

            val value = prop.get(obj)
            val masked = prop.getAnnotation(Masked::class.java)
            val maskedString = if (masked != null && value != null) "***" else value?.toMaskedString()

            s += "${prop.name}=" + maskedString
        }
        clazz = clazz.superclass
    }
    return "${obj.javaClass.simpleName}(${s.joinToString(", ")})"
}

private fun isWhitelisted(clazz: Class<in Any>): Boolean {
    if (clazz.packageName.startsWith("com.hedvig")) {
        return true
    }

    return clazz.name in whitelist
}

private val whitelist = setOf(
    "org.springframework.http.ResponseEntity",
    "org.springframework.http.HttpEntity"
)
