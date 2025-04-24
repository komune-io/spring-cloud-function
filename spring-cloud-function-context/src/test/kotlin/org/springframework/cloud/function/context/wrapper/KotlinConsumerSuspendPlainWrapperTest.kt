package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import org.springframework.core.ResolvableType
import kotlin.Unit
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalStdlibApi::class)
class KotlinConsumerSuspendPlainWrapperTest {

    // Sample suspend consumer function
    private var lastConsumedValue: String? = null

    private val sampleSuspendConsumer: suspend (String) -> Unit = { input ->
        lastConsumedValue = input
    }

    @Test
    fun `test isValid with valid suspend consumer type`() {
        // Given
        val functionType = sampleSuspendConsumer.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Unit>>().javaType,
            typeOf<Unit>().javaType
        )

        // When
        val result = KotlinConsumerSuspendPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid consumer type (flow)`() {
        // Given
        // Create a sample suspend consumer that uses Flow
        val sampleFlowConsumer: suspend (kotlinx.coroutines.flow.Flow<String>) -> Unit = { _ -> }
        val functionType = sampleFlowConsumer.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<kotlinx.coroutines.flow.Flow<String>>().javaType,
            typeOf<kotlin.coroutines.Continuation<Unit>>().javaType,
            typeOf<Unit>().javaType
        )

        // When
        val result = KotlinConsumerSuspendPlainWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Unit>>().javaType,
            typeOf<Unit>().javaType
        )

        // When
        val wrapper = KotlinConsumerSuspendPlainWrapper.asRegistrationFunction(functionName, sampleSuspendConsumer, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test accept method processes input correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Unit>>().javaType,
            typeOf<Unit>().javaType
        )
        val wrapper = KotlinConsumerSuspendPlainWrapper.asRegistrationFunction(functionName, sampleSuspendConsumer, types)
        val input = "test suspend input"
        lastConsumedValue = null

        // When
        wrapper.accept(input)

        // Wait a bit for the async operation to complete
        Thread.sleep(100)

        // Then
        assertThat(lastConsumedValue).isEqualTo(input)
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Consumer::class.java,
            ResolvableType.forClass(String::class.java)
        )

        // When
        val wrapper = KotlinConsumerSuspendPlainWrapper(sampleSuspendConsumer, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
