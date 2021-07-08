package com.hedvig.logging.calls

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
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

    @Pointcut("publicMethod() && beanAnnotatedWithLogExceptions()")
    fun publicMethodInsideAClassMarkedWithLogExceptions() {
    }

    @Before("publicMethodInsideAClassMarkedWithLogExceptions()")
    fun logException(joinPoint: JoinPoint) {
        joinPoint.args
            .filterIsInstance<Throwable>()
            .forEach {
                logger.error(it.localizedMessage, it)
            }
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
