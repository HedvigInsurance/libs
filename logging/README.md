# logging-commons

Useful logging utils.

## `@Mdc` and `@MdcScope`

Functions can be tagged with `@MdcScope`, and its parameters with `@Mdc` in order to populate the
[SLF4J MDC](http://logback.qos.ch/manual/mdc.html) with data, without having to manually put things in the MDC, and
remembering to restore it afterwards.

E.g.
```kotlin
@MdcScope
@GetMapping("/books/{bookId}")
fun getBook(@Mdc @PathVariable bookId: UUID): Book {
    MDC.get("bookId") // will return whatever is in the bookId parameter
} // after returning, the MDC will be restored to its prior state
```

It can also be declared on nested values of types:
```kotlin
data class Book(
    @Mdc val market: String 
)

@MdcScope
@PostMapping("/books")
fun createBook(@RequestBody book: Book) {
    MDC.get("market") // will return the market value of the Book object
}
```

## `@Masked`

To mask out specific fields in data classes or objects, mark the sensitive fields with the `@Masked` annotation and use 
the `obj.toMaskedString()` extension function. Masked fields will be replaced with `***`.

E.g.
```kotlin
data class Hippi(
    @Masked val firstname: String, 
    @Masked val lastname: String, 
    @Masked val ssn: String, 
    val age: Int
)

val a = Hippi("Lars", "Svensson", "710502-0296", 50)

print(a)
print(a.toMaskedStrong())
```
will output:
```
Hippi(firstname=Lars, lastname=Svensson, ssn=710502-0296, age=50)
Hippi(firstname=***, lastname=***, ssn=***, age=50)
```

## `@LogCall`

Mark methods with the `@LogCall` annotation and get all calls to it to be logged on info level.

This uses spring-aop and will only work in Spring applications.

E.g.
```kotlin
class MyClass {
    @LogCall
    fun howdyHo(message: String): String {
        logger.info(message)
        return "Yo!"
    }
}
```
Calling `howdyHo("Glorious days ahead!")` will result in following logs:
```
com.hedvig.MyClass-aop : Executing MyClass.howdyHo(..), parameters: [Glorious days ahead!]
com.hedvig.MyClass-aop : Glorious days ahead!
com.hedvig.MyClass-aop : Executed: MyClass.howdyHo(..), returned: 'Yo!', duration: 0 ms
```
The method parameters and response is masked for fields tagged with the `@Masked` annotation.

## Maven

To use this lib to your project, add following dependency to your `pom.xml`:
