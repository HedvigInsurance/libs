package com.hedvig.libs.logging.mdc

import org.slf4j.MDC

internal class MdcStack(
    private val previousValues: Map<String, String>,
    private val newValues: Map<String, String>
) {
    companion object {
        fun push(values: Map<String, String>): MdcStack {
            val existing = mutableMapOf<String, String>()
            values.forEach { (key, value) ->
                MDC.get(key)?.let { existing[key] = it }
                MDC.put(key, value)
            }
            return MdcStack(existing, values)
        }
    }

    fun restore() {
        newValues.forEach { (key, _) ->
            previousValues[key]?.let { restored ->
                MDC.put(key, restored)
            } ?: run {
                MDC.remove(key)
            }
        }
    }
}