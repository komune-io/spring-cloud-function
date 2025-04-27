package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import org.springframework.core.ResolvableType

/*
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class KotlinSupplierPlainWrapperTest {

    // Sample supplier function that returns a plain value
    private val sampleSupplier: () -> String = {
        "test result"
    }

    @Test
    fun `test isValid with valid supplier type`() {
        // Given
        val functionType = typeOf<() -> String>().javaType
        val types = arrayOf<Type>(typeOf<String>().javaType)

        // When
        val result = KotlinSupplierPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<String>().javaType)

        // When
        val wrapper = KotlinSupplierPlainWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test get method returns correct value`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<String>().javaType)
        val wrapper = KotlinSupplierPlainWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // When
        val result = wrapper.get()

        // Then
        assertThat(result).isEqualTo("test result")
    }

    @Test
    fun `test apply method with empty input returns supplier result`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<String>().javaType)
        val wrapper = KotlinSupplierPlainWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // When
        val result = wrapper.apply(null)

        // Then
        assertThat(result).isEqualTo("test result")
    }

    @Test
    fun `test apply method with non-empty input returns null`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<String>().javaType)
        val wrapper = KotlinSupplierPlainWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // When
        val result = wrapper.apply("some input")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Supplier::class.java,
            ResolvableType.forClass(String::class.java)
        )

        // When
        val wrapper = KotlinSupplierPlainWrapper(sampleSupplier, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
