package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * ## List of Combinations Tested:
 *
 * 1. **Supplier<T>** - No input, single value output (sync) -> supplierPlain
 * 2. **Supplier<Mono<T>>** - No input, single reactive output (Mono) -> supplierMono
 * 3. **Supplier<Flux<T>>** - No input, reactive stream of values -> supplierFlux
 * 4. **Supplier<Message<String>>** - Message output -> supplierMessage
 * 5. **Supplier<Mono<Message<String>>>** - Mono Message output -> supplierMonoMessage
 */
@Configuration
public class SupplierExamples {

	/** 1. Supplier<T> */
	@Bean
	public Supplier<String> supplierPlain() {
		return () -> "Hello, World!";
	}

	/** 2. Supplier<Mono<T>> */
	@Bean
	public Supplier<Mono<String>> supplierMono() {
		return () -> Mono.just("Hello from Mono!");
	}

	/** 3. Supplier<Flux<T>> */
	@Bean
	public Supplier<Flux<String>> supplierFlux() {
		return () -> Flux.just("one", "two", "three");
	}

	/**
	 * 4. Supplier<Message<String>>
	 * Returns a Message<String> with a generated payload and headers.
	 */
	@Bean
	public Supplier<Message<String>> supplierMessage() {
		return () -> MessageBuilder
			.withPayload("Java Message")
			.setHeader("messageId", UUID.randomUUID())
			.setHeader("source", "supplier")
			.build();
	}

	/**
	 * 5. Supplier<Mono<Message<String>>>
	 * Returns a Mono containing a single Message<String>.
	 */
	@Bean
	public Supplier<Mono<Message<String>>> supplierMonoMessage() {
		return () -> Mono.just(
			MessageBuilder
				.withPayload("Java Mono Message")
				.setHeader("javaMonoMsg", true)
				.build()
		).delayElement(Duration.ofMillis(20)); // Optional delay
	}
}
