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
class KotlinConsumerSuspendWrapperTest {

    // Sample suspend consumer function
    private var lastConsumedValue: String? = null

    private val sampleSuspendConsumer: suspend (String) -> Unit = { input ->
        lastConsumedValue = input
    }

    @Test
    fun `test isValid with valid suspend consumer type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendConsumer.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<String>().javaType,
            typeOf<kotlin.coroutines.Continuation<Unit>>().javaType,
            typeOf<Unit>().javaType
        )

        // When
        val result = KotlinConsumerSuspendWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid consumer type (not suspend)`() {
        // Given
        val functionType = typeOf<(String) -> Unit>().javaType
        val types = arrayOf<Type>(typeOf<String>().javaType, typeOf<Unit>().javaType)

        // When
        val result = KotlinConsumerSuspendWrapper.isValid(functionType, types)

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
        val wrapper = KotlinConsumerSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendConsumer, types)

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
        val wrapper = KotlinConsumerSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendConsumer, types)
        val input = "test suspend input"

        // When
        wrapper.accept(input)

        // Then
        assertThat(lastConsumedValue).isEqualTo(input)
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Consumer::class.java,
            ResolvableType.forClassWithGenerics(
                reactor.core.publisher.Flux::class.java,
                ResolvableType.forClass(String::class.java)
            )
        )

        // When
        val wrapper = KotlinConsumerSuspendWrapper(sampleSuspendConsumer, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
