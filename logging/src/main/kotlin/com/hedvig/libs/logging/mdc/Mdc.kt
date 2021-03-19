package com.hedvig.libs.logging.mdc

/**
 * This can be attached to method parameters or class properties to place it inside [org.slf4j.MDC].
 * It must be combined with a [MdcScope] on the method.
 *
 * Currently only supports tagging [String], [Int] and [java.util.UUID].
 *
 *     @GetMapping("/books/{bookId}")
 *     @MdcScope
 *     fun getBook(@Mdc @PathVariable bookId: String): Book {
 *       // code in here will have "bookId": "<bookId value>" set in the MDC
 *     } // after returning, the MDC will be restored to its previous state
 *
 *
 *     @PostMapping
 *     @MdcScope
 *     fun createBook(@RequestBody book: Book) {
 *       // this will have the "bookId" from the Book in the MDC
 *     }
 *
 *     data class Book(
 *       @Mdc val bookId: String  // <-- will be found in the method above
 *     )
 *
 * @param name The property for the MDC value. If empty, the parameter name will be used.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mdc(
    val name: String = ""
)
