package org.springframework.cloud.function.context.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

/**
 * ## List of Combinations Tested (in requested order):
 *  1. (T) -> R                     -> functionPlainToPlain
 *  2. (T) -> Flow<R>               -> functionPlainToFlow
 *  3. (Flow<T>) -> R               -> functionFlowToPlain
 *  4. (Flow<T>) -> Flow<R>         -> functionFlowToFlow
 *  5. suspend (T) -> R             -> functionSuspendPlainToPlain
 *  6. suspend (T) -> Flow<R>       -> functionSuspendPlainToFlow
 *  7. suspend (Flow<T>) -> R       -> functionSuspendFlowToPlain
 *  8. suspend (Flow<T>) -> Flow<R> -> functionSuspendFlowToFlow
 *  9. () -> R                      -> supplierPlain
 *  10. () -> Flow<R>               -> supplierFlow
 *  11. suspend () -> R             -> supplierSuspendPlain
 *  12. suspend () -> Flow<R>       -> supplierSuspendFlow
 *  13. (T) -> Unit                 -> consumerPlain
 *  14. (Flow<T>) -> Unit           -> consumerFlow
 *  15. suspend (T) -> Unit         -> consumerSuspendPlain
 *  16. suspend (Flow<T>) -> Unit   -> consumerSuspendFlow
 *
 *  @author Adrien Poupard
 */
class KotlinTypeValidationTests {

	/* 1. (T) -> R -> functionPlainToPlain */
	@Test
	fun `test functionPlainToPlain`() {
		val (propertyName, type) = Sample.Function::plainToPlain.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.intType
		)
		val isValid = isValidKotlinFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be recognized as (T)->R function.")
			.isTrue()
	}

	/* 2. (T) -> Flow<R> -> functionPlainToFlow */
	@Test
	fun `test functionPlainToFlow`() {
		val (propertyName, type) = Sample.Function::plainToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be (T)->Flow<R>.")
			.isTrue()
	}

	/* 3. (Flow<T>) -> R -> functionFlowToPlain */
	@Test
	fun `test functionFlowToPlain`() {
		val (propertyName, type) = Sample.Function::flowToPlain.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType,
			Sample.Type.intType
		)
		val isValid = isValidKotlinFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be (Flow<T>)->R.")
			.isTrue()
	}

	/* 4. (Flow<T>) -> Flow<R> -> functionFlowToFlow */
	@Test
	fun `test functionFlowToFlow`() {
		val (propertyName, type) = Sample.Function::flowToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowIntType,
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be (Flow<T>)->Flow<R>.")
			.isTrue()
	}

	/* 5. suspend (T) -> R -> functionSuspendPlainToPlain */
	@Test
	fun `test functionSuspendPlainToPlain`() {
		val (propertyName, type) = Sample.Function::suspendPlainToPlain.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.continuationIntType,
			Sample.Type.intType
		)
		val isValid = isValidKotlinSuspendFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (T)->R.")
			.isTrue()
	}

	/* 6. suspend (T) -> Flow<R> -> functionSuspendPlainToFlow */
	@Test
	fun `test functionSuspendPlainToFlow`() {
		val (propertyName, type) = Sample.Function::suspendPlainToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.continuationFlowStringType,
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinSuspendFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (T)->Flow<R>.")
			.isTrue()
	}

	/* 7. suspend (Flow<T>) -> R -> functionSuspendFlowToPlain */
	@Test
	fun `test functionSuspendFlowToPlain`() {
		val (propertyName, type) = Sample.Function::suspendFlowToPlain.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType,
			Sample.Type.continuationIntType,
			Sample.Type.intType
		)
		val isValid = isValidKotlinSuspendFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (Flow<T>)->R.")
			.isTrue()
	}

	/* 8. suspend (Flow<T>) -> Flow<R> -> functionSuspendFlowToFlow */
	@Test
	fun `test functionSuspendFlowToFlow`() {
		val (propertyName, type) = Sample.Function::suspendFlowToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType,
			Sample.Type.continuationFlowStringType,
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinSuspendFunction(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (Flow<T>)->Flow<R>.")
			.isTrue()
	}

	/* 9. () -> R -> supplierPlain */
	@Test
	fun `test supplierPlain`() {
		val (propertyName, type) = Sample.Supplier::unitToPlain.propertyType()

		val isValid = isValidKotlinSupplier(type)

		assertThat(isValid)
			.describedAs("`$propertyName` should be recognized as ()->R supplier.")
			.isTrue()
	}

	/* 10. () -> Flow<R> -> supplierFlow */
	@Test
	fun `test supplierFlow`() {
		val (propertyName, type) = Sample.Supplier::unitToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinSupplier(type)

		assertThat(isValid)
			.describedAs("`$propertyName` should be recognized as ()->Flow<R> supplier.")
			.isTrue()
	}

	/* 11. suspend () -> R -> supplierSuspendPlain */
	@Test
	fun `test supplierSuspendPlain`() {
		val (propertyName, type) = Sample.Supplier::suspendUnitToPlain.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.continuationStringType,
			Sample.Type.stringType
		)
		val isValid = isValidKotlinSuspendSupplier(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be recognized as suspend ()->R supplier.")
			.isTrue()
	}

	/* 12. suspend () -> Flow<R> -> supplierSuspendFlow */
	@Test
	fun `test supplierSuspendFlow`() {
		val (propertyName, type) = Sample.Supplier::suspendUnitToFlow.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.continuationFlowStringType,
			Sample.Type.flowStringType
		)
		val isValid = isValidKotlinSuspendSupplier(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be recognized as suspend ()->Flow<R> supplier.")
			.isTrue()
	}

	/* 13. (T) -> Unit -> consumerPlain */
	@Test
	fun `test consumerPlain`() {
		val (propertyName, type) = Sample.Consumer::plainToUnit.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.unitType
		)
		val isValid = isValidKotlinConsumer(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be (T)->Unit consumer.")
			.isTrue()
	}

	/* 14. (Flow<T>) -> Unit -> consumerFlow */
	@Test
	fun `test consumerFlow`() {
		val (propertyName, type) = Sample.Consumer::flowToUnit.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType,
			Sample.Type.unitType
		)
		val isValid = isValidKotlinConsumer(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be (Flow<T>)->Unit consumer.")
			.isTrue()
	}

	/* 15. suspend (T) -> Unit -> consumerSuspendPlain */
	@Test
	fun `test consumerSuspendPlain`() {
		val (propertyName, type) = Sample.Consumer::suspendPlainToUnit.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.stringType,
			Sample.Type.continuationUnitType,
			Sample.Type.unitType
		)
		val isValid = isValidKotlinSuspendConsumer(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (T)->Unit consumer.")
			.isTrue()
	}

	/* 16. suspend (Flow<T>) -> Unit -> consumerSuspendFlow */
	@Test
	fun `test consumerSuspendFlow`() {
		val (propertyName, type) = Sample.Consumer::suspendFlowToUnit.propertyType()
		val paramTypes = arrayOf(
			Sample.Type.flowStringType,
			Sample.Type.continuationUnitType,
			Sample.Type.unitType
		)
		val isValid = isValidKotlinSuspendConsumer(type, paramTypes)

		assertThat(isValid)
			.describedAs("`$propertyName` should be suspend (Flow<T>)->Unit consumer.")
			.isTrue()
	}

	// -------------------------------------------------------------------
	// Helper: reflect on a property by name and get its Type
	// -------------------------------------------------------------------
	private fun <T> KProperty<T>.propertyType(): Pair<String, Type> {
		val name = this.name
		val field = this.javaField ?: error("No backing field for property $name")
		val actualType = field.genericType
		return name to actualType
	}
}
