package com.hedvig.logging.calls

import com.hedvig.logging.masking.toMaskedString
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LogCallAspect {

    @Suppress("TooGenericExceptionCaught")
    @Around("@annotation(com.hedvig.logging.calls.LogCall)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {

        val start = System.currentTimeMillis()
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringTypeName + "-aop")
        val signature = joinPoint.signature.toShortString()
        val isVoid = (joinPoint.signature as MethodSignature).returnType.name == "void"

        val result = try {
            logger.info("Executing $signature, parameters: [${getParams(joinPoint.args)}]")
            joinPoint.proceed()
        } catch (e: Throwable) {
            logger.info("Exception during executing $signature: $e")
            throw e
        }
        val duration = System.currentTimeMillis() - start
        val resultString = if (isVoid) "-" else result.toMaskedString()

        logger.info("Executed: $signature, returned: '$resultString', duration: $duration ms")

        return result
    }

    private fun getParams(args: Array<Any>) =
        args.map { it.toMaskedString() }
            .joinToString(", ")
}
