/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("CoroutinesUtils")
package org.springframework.cloud.function.context.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import reactor.core.publisher.Mono

/**
 * @author Adrien Poupard
 *
 */
private inline fun <O> executeInCoroutineAndConvertToFlux(crossinline block: (Continuation<O>) -> O): Flux<O> {
	return mono(Dispatchers.Unconfined) {
		suspendCoroutineUninterceptedOrReturn { continuation ->
			block(continuation)
		}
	}.flatMapMany {
		it.convertToFlux()
	}
}

/**
 * Convert a value to a Flux, handling different types appropriately
 *
 * @param value The value to convert
 * @return The value as a Flux
 */
private fun <T> T?.convertToFlux(): Flux<T> {
	return when (this) {
		is Flow<*> -> @Suppress("UNCHECKED_CAST") ((this as Flow<Any>).asFlux() as Flux<T>)
		is Flux<*> -> @Suppress("UNCHECKED_CAST") (this as Flux<T>)
		is Mono<*> -> @Suppress("UNCHECKED_CAST") (this.flatMapMany { Flux.just(it) } as Flux<T>)
		null -> Flux.empty()
		else -> Flux.just(this)
	}
}

fun <I, O> invokeSuspendingFlowFunction(kotlinLambdaTarget: Any, arg0: Flow<I>): Flux<O> {
	@Suppress("UNCHECKED_CAST")
	val function = kotlinLambdaTarget as SuspendFunction<Flow<I>, O>
	return executeInCoroutineAndConvertToFlux { continuation ->
		function.invoke(arg0, continuation)
	}
}

fun <I, O> invokeSuspendingSingleFunction(kotlinLambdaTarget: Any, arg0: I): Flux<O> {
	@Suppress("UNCHECKED_CAST")
	val function = kotlinLambdaTarget as SuspendFunction<I, O>
	return executeInCoroutineAndConvertToFlux { continuation ->
		function.invoke(arg0, continuation)
	}
}

fun <O> invokeSuspendingSupplier(kotlinLambdaTarget: Any): Flux<O> {
	@Suppress("UNCHECKED_CAST")
	val supplier = kotlinLambdaTarget as SuspendSupplier<O>
	return executeInCoroutineAndConvertToFlux { continuation ->
		supplier.invoke(continuation)
	}
}

fun <I> invokeSuspendingConsumer(kotlinLambdaTarget: Any, arg0: I) {
	@Suppress("UNCHECKED_CAST")
	val consumer = kotlinLambdaTarget as SuspendConsumer<I>
	executeInCoroutineAndConvertToFlux { continuation ->
		// FIXME: This is a fix for KotlinConsumerSuspendWrapperTest  fun `test accept method processes input correctly`() {
		when (arg0) {
			is Flux<*> -> {
				val flow = (arg0 as Flux<*>).asFlow()
				@Suppress("UNCHECKED_CAST")
				consumer.invoke(flow as I, continuation)
			}
			else -> consumer.invoke(arg0, continuation)
		}
	}.subscribe()
}

private typealias SuspendFunction<I, O> = (I?, Continuation<O>) -> O

private typealias SuspendConsumer<I> = (I?, Continuation<Unit>) -> Unit?

private typealias SuspendSupplier<O> = (Continuation<O>) -> O
