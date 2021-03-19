package com.hedvig.libs.logging.mdc

/**
 * This can be attached to a function to make it a scope for [org.slf4j.MDC] data, which can be declared and read
 * directly from annotated parameters of that function.
 *
 * Methods tagged with [MdcScope] will search for parameters or nested properties of the parameters for the [Mdc]
 * annotation,
 *
 *     @GetMapping("/books/{bookId}")
 *     @MdcScope
 *     fun getBook(@Mdc @PathVariable bookId: String): Book {
 *       // code in here will have "bookId": "<bookId value>" set in the MDC
 *     } // after returning, the MDC will be restored to its previous state
 *
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MdcScope

