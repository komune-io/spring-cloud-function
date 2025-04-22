package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@OptIn(ExperimentalStdlibApi::class)
class KotlinFunctionSuspendFlowToFlowWrapperTest {

    // Sample suspend function that transforms a Flow to another Flow
    private val sampleSuspendFunction: suspend (Flow<String>) -> Flow<String> = { flow ->
        flow.map { it.uppercase() }
    }

    @Test
    fun `test isValid with valid suspend flow to flow function type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendFunction.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<String>>>().javaType,
            typeOf<Flow<String>>().javaType
        )

        // When
        val result = KotlinFunctionSuspendFlowToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid function type (not suspend flow to flow)`() {
        // Given
        val functionType = typeOf<(Flow<String>) -> Flow<String>>().javaType
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<Flow<String>>().javaType
        )

        // When
        val result = KotlinFunctionSuspendFlowToFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<String>>>().javaType,
            typeOf<Flow<String>>().javaType
        )

        // When
        val wrapper = KotlinFunctionSuspendFlowToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)

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
            typeOf<kotlin.coroutines.Continuation<Flow<String>>>().javaType,
            typeOf<Flow<String>>().javaType
        )
        val wrapper = KotlinFunctionSuspendFlowToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val inputFlux = Flux.just("test1", "test2", "test3") as Flux<Any>

        // When
        val resultFlux = wrapper.apply(inputFlux)

        // Then
        StepVerifier.create(resultFlux)
            .expectNext("TEST1")
            .expectNext("TEST2")
            .expectNext("TEST3")
            .verifyComplete()
    }

    @Test
    fun `test invoke method processes Flux correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Flow<String>>>().javaType,
            typeOf<Flow<String>>().javaType
        )
        val wrapper = KotlinFunctionSuspendFlowToFlowWrapper.asRegistrationFunction(functionName, sampleSuspendFunction, types)
        val inputFlux = Flux.just("test4", "test5", "test6") as Flux<Any>

        // When
        val resultFlux = wrapper.invoke(inputFlux)

        // Then
        StepVerifier.create(resultFlux)
            .expectNext("TEST4")
            .expectNext("TEST5")
            .expectNext("TEST6")
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
            ResolvableType.forClassWithGenerics(
                reactor.core.publisher.Flux::class.java,
                ResolvableType.forClass(String::class.java)
            )
        )

        // When
        val wrapper = KotlinFunctionSuspendFlowToFlowWrapper(sampleSuspendFunction, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
