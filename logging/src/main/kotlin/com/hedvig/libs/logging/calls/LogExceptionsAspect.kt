package com.hedvig.logging.calls

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

/**
 * Logs exception parameters for public methods in classes annotated with @LogExceptions.
 * Intended to be used on subclasses of ResponseEntityExceptionHandler.
 */
@Aspect
@Component
class LogExceptionsAspect {

    @Pointcut("within(@com.hedvig.logging.calls.LogExceptions *)")
    fun beanAnnotatedWithLogExceptions() {
    }

    @Pointcut("execution(public * *(..))")
    fun publicMethod() {
    }

    @Pointcut("execution(protected * *(..))")
    fun protectedMethod() {
    }

    @Pointcut("(publicMethod() || protectedMethod()) && beanAnnotatedWithLogExceptions()")
    fun condition() {
    }

    @AfterReturning(pointcut = "condition()", returning = "returnValue")
    fun logException(joinPoint: JoinPoint, returnValue: Any?) {
        val ex = joinPoint.args.filterIsInstance<Throwable>().firstOrNull() ?: return
        if (returnValue is ResponseEntity<*>) {
            when {
                returnValue.statusCode.is5xxServerError -> logger.error(ex.localizedMessage, ex)
                returnValue.statusCode.is4xxClientError -> logger.info(ex.localizedMessage, ex)
                else -> logger.warn(ex.localizedMessage, ex)
            }
        } else {
            logger.warn(ex.localizedMessage, ex)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
