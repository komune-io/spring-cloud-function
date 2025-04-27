package org.springframework.cloud.function.context.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.coroutines.Continuation
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import org.springframework.core.ResolvableType

/**
 * Tests for TypeUtils.kt utility functions.
 *
 * @author Adrien Poupard
 */
@OptIn(ExperimentalStdlibApi::class)
class TypeUtilsTests {

    /**
     * Test for the getContinuationTypeArguments method.
     * 
     * This test verifies that when a non-continuation type is passed to getContinuationTypeArguments,
     * it returns the original type.
     */
    @Test
    fun `test with non-continuation type`() {
        // Given a non-continuation type
        val type = Sample.Type.stringType

        // When getContinuationTypeArguments is called directly
        val result = getContinuationTypeArguments(type)

        // Then it should return the original type
        assertThat(result.typeName).isEqualTo("java.lang.String")
    }

    /**
     * Test for the getContinuationTypeArguments method.
     * 
     * This test verifies that when a continuation type with a simple type argument is passed,
     * it correctly extracts the type argument.
     */
    @Test
    fun `test with simple continuation type`() {
        // Given a continuation type with a simple type argument
        val type = Sample.Type.continuationStringType

        // When getContinuationTypeArguments is called directly
        val result = getContinuationTypeArguments(type)

        // Then it should extract the type argument
        assertThat(result.typeName).isEqualTo("java.lang.String")
    }

    /**
     * Test for the getContinuationTypeArguments method.
     * 
     * This test verifies that when a continuation type with a parameterized type argument is passed,
     * it correctly extracts the parameterized type.
     */
    @Test
    fun `test with parameterized continuation type`() {
        // Given a continuation type with a parameterized type argument (Flow<String>)
        val type = Sample.Type.continuationFlowStringType

        // When getContinuationTypeArguments is called directly
        val result = getContinuationTypeArguments(type)

        // Then it should extract the parameterized type (Flow<String>)
        assertThat(result.typeName).startsWith("kotlinx.coroutines.flow.Flow")
        assertThat(result.typeName).contains("String")
    }

    /**
     * Test for the getContinuationTypeArguments method with wildcard type handling.
     * 
     * This test creates a mock continuation type with a wildcard type argument to test
     * the wildcard type handling in getContinuationTypeArguments.
     */
    @Test
    fun `test with wildcard type handling`() {
        // Given a mock continuation type with a wildcard type
        val mockWildcardType = object : WildcardType {
            override fun getLowerBounds(): Array<Type> = arrayOf(Sample.Type.stringType)
            override fun getUpperBounds(): Array<Type> = arrayOf(Any::class.java)
            override fun getTypeName(): String = "? super java.lang.String"
        }

        val mockContinuationType = object : ParameterizedType {
            override fun getRawType(): Type = Continuation::class.java
            override fun getOwnerType(): Type? = null
            override fun getActualTypeArguments(): Array<Type> = arrayOf(mockWildcardType)
            override fun getTypeName(): String = "kotlin.coroutines.Continuation<? super java.lang.String>"
        }

        // When getContinuationTypeArguments is called with our mock type
        val result = getContinuationTypeArguments(mockContinuationType)

        // Then it should extract the lower bound of the wildcard type
        assertThat(result.typeName).isEqualTo("java.lang.String")
    }

    /**
     * Test for the getFlowTypeArguments method.
     * 
     * This test verifies that when a non-flow type is passed to getFlowTypeArguments,
     * it returns the original type.
     */
    @Test
    fun `test getFlowTypeArguments with non-flow type`() {
        // Given a non-flow type
        val type = Sample.Type.stringType

        // When getFlowTypeArguments is called
        val result = getFlowTypeArguments(type)

        // Then it should return the original type
        assertThat(result.typeName).isEqualTo("java.lang.String")
    }

    /**
     * Test for the getFlowTypeArguments method.
     * 
     * This test verifies that when a flow type with a simple type argument is passed,
     * it correctly extracts the type argument.
     */
    @Test
    fun `test getFlowTypeArguments with simple flow type`() {
        // Given a flow type with a simple type argument
        val type = Sample.Type.flowStringType

        // When getFlowTypeArguments is called
        val result = getFlowTypeArguments(type)

        // Then it should extract the type argument
        assertThat(result.typeName).isEqualTo("java.lang.String")
    }

    /**
     * Test for the getFlowTypeArguments method with wildcard type handling.
     * 
     * This test creates a mock flow type with a wildcard type argument to test
     * the wildcard type handling in getFlowTypeArguments.
     */
    @Test
    fun `test getFlowTypeArguments with wildcard type`() {
        // Given a mock flow type with a wildcard type
        val mockWildcardType = object : WildcardType {
            override fun getLowerBounds(): Array<Type> = arrayOf(Sample.Type.stringType)
            override fun getUpperBounds(): Array<Type> = arrayOf(Any::class.java)
            override fun getTypeName(): String = "? extends java.lang.Object"
        }

        val mockFlowType = object : ParameterizedType {
            override fun getRawType(): Type = Flow::class.java
            override fun getOwnerType(): Type? = null
            override fun getActualTypeArguments(): Array<Type> = arrayOf(mockWildcardType)
            override fun getTypeName(): String = "kotlinx.coroutines.flow.Flow<? extends java.lang.Object>"
        }

        // When getFlowTypeArguments is called with our mock type
        val result = getFlowTypeArguments(mockFlowType)

        // Then it should extract the upper bound of the wildcard type
        assertThat(result.typeName).isEqualTo("java.lang.Object")
    }

    /**
     * Test for the isFlowType method.
     * 
     * This test verifies that isFlowType correctly identifies Flow types.
     */
    @Test
    fun `test isFlowType`() {
        // Given a flow type and a non-flow type
        val flowType = Sample.Type.flowStringType
        val nonFlowType = Sample.Type.stringType

        // When isFlowType is called
        val flowResult = isFlowType(flowType)
        val nonFlowResult = isFlowType(nonFlowType)

        // Then it should correctly identify the flow type
        assertThat(flowResult).isTrue()
        assertThat(nonFlowResult).isFalse()
    }

    /**
     * Test for the isContinuationType method.
     * 
     * This test verifies that isContinuationType correctly identifies Continuation types.
     */
    @Test
    fun `test isContinuationType`() {
        // Given a continuation type and a non-continuation type
        val continuationType = Sample.Type.continuationStringType
        val nonContinuationType = Sample.Type.stringType

        // When isContinuationType is called
        val continuationResult = isContinuationType(continuationType)
        val nonContinuationResult = isContinuationType(nonContinuationType)

        // Then it should correctly identify the continuation type
        assertThat(continuationResult).isTrue()
        assertThat(nonContinuationResult).isFalse()
    }

    /**
     * Test for the isUnitType method.
     * 
     * This test verifies that isUnitType correctly identifies Unit types.
     */
    @Test
    fun `test isUnitType`() {
        // Given a unit type and a non-unit type
        val unitType = Sample.Type.unitType
        val nonUnitType = Sample.Type.stringType

        // When isUnitType is called
        val unitResult = isUnitType(unitType)
        val nonUnitResult = isUnitType(nonUnitType)

        // Then it should correctly identify the unit type
        assertThat(unitResult).isTrue()
        assertThat(nonUnitResult).isFalse()
    }

    /**
     * Test for the isContinuationUnitType method.
     * 
     * This test verifies that isContinuationUnitType correctly identifies Continuation<Unit> types.
     */
    @Test
    fun `test isContinuationUnitType`() {
        // Given a continuation unit type and a non-continuation unit type
        val continuationUnitType = Sample.Type.continuationUnitType
        val continuationNonUnitType = Sample.Type.continuationStringType

        // When isContinuationUnitType is called
        val continuationUnitResult = isContinuationUnitType(continuationUnitType)
        val continuationNonUnitResult = isContinuationUnitType(continuationNonUnitType)

        // Then it should correctly identify the continuation unit type
        assertThat(continuationUnitResult).isTrue()
        assertThat(continuationNonUnitResult).isFalse()
    }

    /**
     * Test for the isContinuationFlowType method.
     * 
     * This test verifies that isContinuationFlowType correctly identifies Continuation<Flow<*>> types.
     */
    @Test
    fun `test isContinuationFlowType`() {
        // Given a continuation flow type and a non-continuation flow type
        val continuationFlowType = Sample.Type.continuationFlowStringType
        val continuationNonFlowType = Sample.Type.continuationStringType

        // When isContinuationFlowType is called
        val continuationFlowResult = isContinuationFlowType(continuationFlowType)
        val continuationNonFlowResult = isContinuationFlowType(continuationNonFlowType)

        // Then it should correctly identify the continuation flow type
        assertThat(continuationFlowResult).isTrue()
        assertThat(continuationNonFlowResult).isFalse()
    }
}
