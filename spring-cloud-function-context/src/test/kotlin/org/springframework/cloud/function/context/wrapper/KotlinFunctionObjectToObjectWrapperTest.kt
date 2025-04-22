package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import org.springframework.core.ResolvableType

@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionObjectToObjectWrapperTest {

    // Sample function that transforms a String to an Int
    private val sampleFunction: (String) -> Int = { input ->
        input.length
    }

    @Test
    fun `test isValid with valid object to object function type`() {
        // Given
        val functionType = typeOf<(String) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val result = KotlinFunctionObjectToObjectWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val functionType = typeOf<(String) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val wrapper = KotlinFunctionObjectToObjectWrapper.asRegistrationFunction(
            functionName, 
            sampleFunction, 
            functionType,
            types
        )

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test apply method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val functionType = typeOf<(String) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionObjectToObjectWrapper.asRegistrationFunction(
            functionName, 
            sampleFunction, 
            functionType,
            types
        )
        val input = "test input"

        // When
        val result = wrapper.apply(input)

        // Then
        assertThat(result).isEqualTo(10) // "test input".length = 10
    }

    @Test
    fun `test invoke method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val functionType = typeOf<(String) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionObjectToObjectWrapper.asRegistrationFunction(
            functionName, 
            sampleFunction, 
            functionType,
            types
        )
        val input = "another test"

        // When
        val result = wrapper.invoke(input)

        // Then
        assertThat(result).isEqualTo(12) // "another test".length = 12
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Function::class.java,
            ResolvableType.forClass(String::class.java),
            ResolvableType.forClass(Int::class.java)
        )
        val isSuspendFunction = false

        // When
        val wrapper = KotlinFunctionObjectToObjectWrapper(sampleFunction, type, functionName, isSuspendFunction)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}