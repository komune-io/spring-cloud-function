package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import org.springframework.core.ResolvableType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

/*
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class KotlinSupplierSuspendWrapperTest {

    // Sample suspend supplier function
    private val sampleSuspendSupplier: suspend () -> String = {
        "test suspend result"
    }

    @Test
    fun `test isValid with valid suspend supplier type`() {
        // Given
        // Use reflection to get the type instead of typeOf<suspend ...>().javaType
        val functionType = sampleSuspendSupplier.javaClass.genericInterfaces[0]
        val types = arrayOf<Type>(
            typeOf<kotlin.coroutines.Continuation<String>>().javaType,
            typeOf<String>().javaType
        )

        // When
        val result = KotlinSupplierSuspendWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test isValid with invalid supplier type (not suspend)`() {
        // Given
        val functionType = typeOf<() -> String>().javaType
        val types = arrayOf<Type>(typeOf<String>().javaType)

        // When
        val result = KotlinSupplierSuspendWrapper.isValid(functionType, types)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test asRegistrationFunction creates wrapper correctly`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<kotlin.coroutines.Continuation<String>>().javaType,
            typeOf<String>().javaType
        )

        // When
        val wrapper = KotlinSupplierSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendSupplier, types)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isNotNull
    }

    @Test
    fun `test get method returns Flux with correct value`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<kotlin.coroutines.Continuation<String>>().javaType,
            typeOf<String>().javaType
        )
        val wrapper = KotlinSupplierSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendSupplier, types)

        // When
        val result = wrapper.get()

        // Then
        assertThat(result).isInstanceOf(Flux::class.java)

        // Verify the content of the Flux
        StepVerifier.create(result as Flux<*>)
            .expectNext("test suspend result")
            .verifyComplete()
    }

    @Test
    fun `test apply method with empty input returns supplier result`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<kotlin.coroutines.Continuation<String>>().javaType,
            typeOf<String>().javaType
        )
        val wrapper = KotlinSupplierSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendSupplier, types)

        // When
        val result = wrapper.apply(null)

        // Then
        assertThat(result).isInstanceOf(Flux::class.java)

        // Verify the content of the Flux
        StepVerifier.create(result as Flux<*>)
            .expectNext("test suspend result")
            .verifyComplete()
    }

    @Test
    fun `test apply method with non-empty input returns null`() {
        // Given
        val functionName = "testFunction"
        val types = arrayOf<Type>(
            typeOf<kotlin.coroutines.Continuation<String>>().javaType,
            typeOf<String>().javaType
        )
        val wrapper = KotlinSupplierSuspendWrapper.asRegistrationFunction(functionName, sampleSuspendSupplier, types)

        // When
        val result = wrapper.apply("some input")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `test constructor with type parameter`() {
        // Given
        val functionName = "testFunction"
        val type = ResolvableType.forClassWithGenerics(
            java.util.function.Supplier::class.java,
            ResolvableType.forClassWithGenerics(
                reactor.core.publisher.Flux::class.java,
                ResolvableType.forClass(String::class.java)
            )
        )

        // When
        val wrapper = KotlinSupplierSuspendWrapper(sampleSuspendSupplier, type, functionName)

        // Then
        assertThat(wrapper).isNotNull
        assertThat(wrapper.getName()).isEqualTo(functionName)
        assertThat(wrapper.getResolvableType()).isEqualTo(type)
    }
}
