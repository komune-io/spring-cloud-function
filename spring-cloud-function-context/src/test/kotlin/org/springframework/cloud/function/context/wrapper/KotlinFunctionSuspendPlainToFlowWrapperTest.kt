package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionSuspendPlainToFlowWrapperTest {

    // Sample suspend function that transforms a String to a Flow of characters
    private val sampleSuspendFunction: suspend (String) -> Flow<Char> = { input ->
        flow {
            input.forEach { emit(it) }
        }
    }

    @Test
    fun `test isValid with valid suspend plain to flow function type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendFunction.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<Char>>>().javaType,
            typeOf<Flow<Char>>().javaType
        )

        // When
        val result = KotlinFunctionSuspendPlainToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not suspend plain to flow)`() {
        // Given
        // For the invalid test, we can use a non-suspend function type instead
        val functionType = typeOf<(String) -> String>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<String>().javaType
        )

        // When
        val result = KotlinFunctionSuspendPlainToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<Char>>>().javaType,
            typeOf<Flow<Char>>().javaType
        )

        // When
        val wrapper = KotlinFunctionSuspendPlainToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)

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
            typeOf<kotlin.coroutines.Continuation<Flow<Char>>>().javaType,
            typeOf<Flow<Char>>().javaType
        )
        val wrapper = KotlinFunctionSuspendPlainToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val input = "test"

        // When
        val resultFlux = wrapper.apply(input)

        // Then
        StepVerifier.create(resultFlux)
            .expectNext('t')
            .expectNext('e')
            .expectNext('s')
            .expectNext('t')
            .verifyComplete()
    }

    @Test
    fun `test invoke method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<Char>>>().javaType,
            typeOf<Flow<Char>>().javaType
        )
        val wrapper = KotlinFunctionSuspendPlainToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val input = "abc"

        // When
        val resultFlux = wrapper.invoke(input)

        // Then
        StepVerifier.create(resultFlux)
            .expectNext('a')
            .expectNext('b')
            .expectNext('c')
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
                ResolvableType.forClass(Char::class.java)
            )
        )

        // When
        val wrapper = KotlinFunctionSuspendPlainToFlowWrapper(sampleSuspendFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
