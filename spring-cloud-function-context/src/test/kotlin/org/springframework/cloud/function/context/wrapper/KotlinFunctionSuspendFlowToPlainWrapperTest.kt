package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

/*
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionSuspendFlowToPlainWrapperTest {

    // Sample suspend function that transforms a Flow to a plain value
    private val sampleSuspendFunction: suspend (Flow<String>) -> Int = { flow ->
        flow.count()
    }

    @Test
    fun `test isValid with valid suspend flow to plain function type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendFunction.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val result = KotlinFunctionSuspendFlowToPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not suspend flow to plain)`() {
        // Given
        // For the invalid test, we can use a non-suspend function type instead
        val functionType = typeOf<(Flow<String>) -> Flow<String>>().javaType
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<Flow<String>>().javaType
        )

        // When
        val result = KotlinFunctionSuspendFlowToPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )

        // When
        val wrapper = KotlinFunctionSuspendFlowToPlainWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test apply method processes Flux correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionSuspendFlowToPlainWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val inputFlux = Flux.just("test1", "test2", "test3") as Flux<Any>

        // When
        val result = wrapper.apply(inputFlux)

        // Then
        // The result is a Flux that emits a single value (3)
        StepVerifier.create(result as Flux<*>)
            .expectNext(3)
            .verifyComplete()
    }

    @Test
    fun `test invoke method processes Flux correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Int>>().javaType,
            typeOf<Int>().javaType
        )
        val wrapper = KotlinFunctionSuspendFlowToPlainWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val inputFlux = Flux.just("test4", "test5", "test6", "test7") as Flux<Any>

        // When
        val result = wrapper.invoke(inputFlux)

        // Then
        // The result is a Flux that emits a single value (4)
        StepVerifier.create(result as Flux<*>)
            .expectNext(4)
            .verifyComplete()
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
        val wrapper = KotlinFunctionSuspendFlowToPlainWrapper(sampleSuspendFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
