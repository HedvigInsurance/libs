package com.hedvig.libs.logging.mdc

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.util.*

internal class MdcTest {

    @Test
    fun `no mdc method should product empty map`() {
        val mdc = extractMdcProperties(this::emptyMethod, emptyArray())

        assertThat(mdc).isEmpty()
    }

    @Test
    fun `one mdc value - should get name of property`() {
        val mdc = extractMdcProperties(this::singleMdcValue, arrayOf("Person"))

        assertThat(mdc).isEqualTo(mapOf("firstName" to "Person"))
    }

    @Test
    fun `one mdc value - with property name override`() {
        val mdc = extractMdcProperties(this::singleMdcValueWithCustomName, arrayOf("Person"))

        assertThat(mdc).isEqualTo(mapOf("first_name" to "Person"))
    }

    @Test
    fun `two mdc values`() {
        val mdc = extractMdcProperties(this::twoMdcValues, arrayOf("Person", "Lastname"))

        assertThat(mdc).isEqualTo(mapOf("firstName" to "Person", "lastName" to "Lastname"))
    }

    @Test
    fun `one mdc value - but others are not mdc`() {
        val uuid = UUID.randomUUID()
        val mdc = extractMdcProperties(this::singleMdcAmongNonMdc, arrayOf(10, uuid, "Testing"))

        assertThat(mdc).isEqualTo(mapOf("id" to uuid.toString()))
    }

    @Test
    fun `nested mdc value`() {
        val mdc = extractMdcProperties(this::singleRequestObject, arrayOf(Request("1234")))

        assertThat(mdc).isEqualTo(mapOf("targetId" to "1234"))
    }

    @Test
    fun `mdc is stringified`() {
        val mdc = extractMdcProperties(this::singleIntValue, arrayOf(17))

        assertThat(mdc).isEqualTo(mapOf("age" to "17"))
    }

    @Test
    fun `test unsupported type`() {
        assertThrows<IllegalArgumentException> {
            extractMdcProperties(this::misstaggedParameter, arrayOf(Request("1234")))
        }
    }

    private fun emptyMethod() {}
    private fun singleMdcValue(@Mdc firstName: String) {}
    private fun singleMdcValueWithCustomName(@Mdc("first_name") firstName: String) {}
    private fun twoMdcValues(@Mdc firstName: String, @Mdc lastName: String) {}
    private fun singleMdcAmongNonMdc(age: Int, @Mdc id: UUID, name: String) {}
    private fun singleIntValue(@Mdc age: Int) {}
    private fun singleRequestObject(request: Request) {}
    private fun misstaggedParameter(@Mdc request: Request) {}

    data class Request(
        @Mdc val targetId: String
    )
}
