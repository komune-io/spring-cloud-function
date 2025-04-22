package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionSuspendObjectToObjectWrapperTest {

    // Sample suspend function that transforms a String to an Int
    private val sampleSuspendFunction: suspend (String) -> Int = { input ->
        input.length
    }

    @Test
    fun `test isValid with valid suspend object to object function type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendFunction.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val result = KotlinFunctionSuspendObjectToObjectWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not suspend)`() {
        // Given
        val functionType = typeOf<(String) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val result = KotlinFunctionSuspendObjectToObjectWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val wrapper = KotlinFunctionSuspendObjectToObjectWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test apply method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionSuspendObjectToObjectWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val input = "test input"

        // When
        val result = wrapper.apply(input)

        // Then
        // The result should be a Flux containing the length of the input string
        StepVerifier.create(result as Flux<*>)
            .expectNext(10) // "test input".length = 10
            .verifyComplete()
    }

    @Test
    fun `test invoke method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionSuspendObjectToObjectWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val input = "another test"

        // When
        val result = wrapper.invoke(input)

        // Then
        // The result should be a Flux containing the length of the input string
        StepVerifier.create(result as Flux<*>)
            .expectNext(12) // "another test".length = 12
            .verifyComplete()
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Function::class.java,
            ResolvableType.forClass(String::class.java),
            ResolvableType.forClassWithGenerics(
                reactor.core.publisher.Flux::class.java,
                ResolvableType.forClass(Int::class.java)
            )
        )

        // When
        val wrapper = KotlinFunctionSuspendObjectToObjectWrapper(sampleSuspendFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
