package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

/*
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionPlainToFlowWrapperTest {

    // Sample function that transforms a String to a Flow of characters
    private val sampleFunction: (String) -> Flow<Char> = { input ->
        flow {
            input.forEach { emit(it) }
        }
    }

    @Test
    fun `test isValid with valid plain to flow function type`() {
        // Given
        val functionType = typeOf<(String) -> Flow<Char>>().javaType
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Flow<Char>>().javaType
        )

        // When
        val result = KotlinFunctionPlainToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not plain to flow)`() {
        // Given
        val functionType = typeOf<(Flow<String>) -> String>().javaType
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<String>().javaType
        )

        // When
        val result = KotlinFunctionPlainToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<Flow<Char>>().javaType
        )

        // When
        val wrapper = KotlinFunctionPlainToFlowWrapper.asRegistrationFunction(functionName, sampleFunction, types)

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
            typeOf<Flow<Char>>().javaType
        )
        val wrapper = KotlinFunctionPlainToFlowWrapper.asRegistrationFunction(functionName, sampleFunction, types)
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
            typeOf<Flow<Char>>().javaType
        )
        val wrapper = KotlinFunctionPlainToFlowWrapper.asRegistrationFunction(functionName, sampleFunction, types)
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
        val wrapper = KotlinFunctionPlainToFlowWrapper(sampleFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
