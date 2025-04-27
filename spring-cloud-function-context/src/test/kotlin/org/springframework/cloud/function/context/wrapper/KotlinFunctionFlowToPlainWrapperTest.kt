package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux

/*
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionFlowToPlainWrapperTest {

    // Sample function that transforms a Flow to a plain value
    private val sampleFunction: (Flow<String>) -> Int = { flow ->
        runBlocking { flow.count() }
    }

    @Test
    fun `test isValid with valid flow to plain function type`() {
        // Given
        val functionType = typeOf<(Flow<String>) -> Int>().javaType
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val result = KotlinFunctionFlowToPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not flow to plain)`() {
        // Given
        val functionType = typeOf<(Flow<String>) -> Flow<String>>().javaType
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<Flow<String>>().javaType
        )

        // When
        val result = KotlinFunctionFlowToPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf(
            typeOf<Flow<String>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val wrapper = KotlinFunctionFlowToPlainWrapper.asRegistrationFunction(functionName, sampleFunction, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test apply method processes Flux correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf(
            typeOf<Flow<String>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionFlowToPlainWrapper.asRegistrationFunction(functionName, sampleFunction, types)
        val inputFlux = Flux.just("test1", "test2", "test3") as Flux<Any>

        // When
        val result = wrapper.apply(inputFlux)

        // Then
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `test invoke method processes Flux correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionFlowToPlainWrapper.asRegistrationFunction(functionName, sampleFunction, types)
        val inputFlux = Flux.just("test4", "test5", "test6", "test7") as Flux<Any>

        // When
        val result = wrapper.invoke(inputFlux)

        // Then
        assertThat(result).isEqualTo(4)
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Function::class.java,
            ResolvableType.forClassWithGenerics(
                reactor.core.publisher.Flux::class.java,
                ResolvableType.forClass(String::class.java)
            ),
            ResolvableType.forClass(Int::class.java)
        )

        // When
        val wrapper = KotlinFunctionFlowToPlainWrapper(sampleFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
