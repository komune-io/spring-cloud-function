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

@OptIn(ExperimentalStdlibApi::class)
class KotlinSupplierFlowWrapperTest {

    // Sample supplier function that returns a Flow
    private val sampleSupplier: () -> Flow<String> = {
        flow {
            emit("test1")
            emit("test2")
            emit("test3")
        }
    }

    @Test
    fun `test isValid with valid supplier flow type`() {
        // Given
        val functionType = typeOf<() -> Flow<String>>().javaType
        val types = arrayOf<Type>(typeOf<Flow<String>>().javaType)

        // When
        val result = KotlinSupplierFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid supplier type`() {
        // Given
        val functionType = typeOf<() -> String>().javaType
        val types = arrayOf<Type>(typeOf<String>().javaType)

        // When
        val result = KotlinSupplierFlowWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<Flow<String>>().javaType)

        // When
        val wrapper = KotlinSupplierFlowWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // Then
		assertThat(wrapper != null).isTrue()
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test get method converts Flow to Flux`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<Flow<String>>().javaType)
        val wrapper = KotlinSupplierFlowWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // When
        val result = wrapper.get()

        // Then
        assertThat(result).isInstanceOf(Flux::class.java)

        // Verify the content of the Flux
        StepVerifier.create(result)
            .expectNext("test1")
            .expectNext("test2")
            .expectNext("test3")
            .verifyComplete()
    }

    @Test
    fun `test invoke method returns Flow`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(typeOf<Flow<String>>().javaType)
        val wrapper = KotlinSupplierFlowWrapper.asRegistrationFunction(functionName, sampleSupplier, types)

        // When
        val result = wrapper.invoke()

        // Then
        assertThat(result).isNotEqualTo(null)

        // Verify the content of the Flow
        runBlocking {
            val items = result.toList()
            assertThat(items).containsExactly("test1", "test2", "test3")
        }
    }
}
