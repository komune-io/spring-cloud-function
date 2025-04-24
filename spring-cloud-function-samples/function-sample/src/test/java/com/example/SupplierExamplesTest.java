package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

/**
 * Test class for verifying the Java Supplier examples in [SupplierExamples].
 * Each bean is exposed at "/{beanName}" by Spring Cloud Function.
 *
 * ## Suppliers Tested:
 * 1. **Supplier<T>** - No input, single value output (sync) -> supplierPlain
 * 2. **Supplier<Mono<T>>** - No input, single reactive output (Mono) -> supplierMono
 * 3. **Supplier<Flux<T>>** - No input, reactive stream of values -> supplierFlux
 * 4. **Supplier<Message<String>>** - Message output -> supplierMessage
 * 5. **Supplier<Mono<Message<String>>>** - Mono Message output -> supplierMonoMessage
 */
@FunctionalSpringBootTest
@AutoConfigureWebTestClient
public class SupplierExamplesTest {

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void setup() {
		this.webTestClient = webTestClient.mutate()
			.responseTimeout(Duration.ofSeconds(120))
			.build();
	}

	/**
	 * 1. Supplier<T> -> supplierPlain
	 * Supplier that returns a single String value.
	 *
	 * --- Input: ---
	 * GET /supplierPlain
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * Content-Type: text/plain (usually default for single String)
	 * "Hello, World!"
	 */
	@Test
	void testSupplierPlain() {
		webTestClient.get()
			.uri("/supplierPlain")
			.exchange()
			.expectStatus().isOk()
			//.expectHeader().contentType(MediaType.TEXT_PLAIN) // Optional: Verify exact type
			.expectBody(String.class)
			.isEqualTo("Hello, World!");
	}

	/**
	 * 2. Supplier<Mono<T>> -> supplierMono
	 * Supplier that returns a single reactive Mono<String>.
	 *
	 * --- Input: ---
	 * GET /supplierMono
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * Content-Type: text/plain (usually default for single String from Mono)
	 * "Hello from Mono!"
	 */
	@Test
	void testSupplierMono() {
		webTestClient.get()
			.uri("/supplierMono")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.isEqualTo("Hello from Mono!");
	}

	/**
	 * 3. Supplier<Flux<T>> -> supplierFlux
	 * Supplier that returns a reactive stream (Flux<String>) of values.
	 *
	 * --- Input: ---
	 * GET /supplierFlux
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * Content-Type: application/json
	 * ["one", "two", "three"]
	 */
	@Test
	void testSupplierFlux() {
		webTestClient.get()
			.uri("/supplierFlux")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON) // Flux usually defaults to JSON array
			.expectBody(new ParameterizedTypeReference<List<String>>() {})
			.isEqualTo(List.of("one", "two", "three"));
	}

	/**
	 * 4. Supplier<Message<String>> -> supplierMessage
	 * Returns a Message<String> with a generated payload and headers.
	 *
	 * --- Input: ---
	 * GET /supplierMessage
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * Header: messageId=<uuid>
	 * Header: source=supplier
	 * Content-Type: text/plain (usually default for single String payload)
	 * "Java Message"
	 */
	@Test
	void testSupplierMessage() {
		webTestClient.get()
			.uri("/supplierMessage")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().exists("messageId") // Check headers
			.expectHeader().valueEquals("source", "supplier")
			.expectBody(String.class) // Check payload
			.isEqualTo("Java Message");
	}

	/**
	 * 5. Supplier<Mono<Message<String>>> -> supplierMonoMessage
	 * Returns a Mono containing a single Message<String>.
	 *
	 * --- Input: ---
	 * GET /supplierMonoMessage
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * Header: javaMonoMsg=true
	 * Header: id=<uuid>
	 * Content-Type: text/plain (usually default for single String payload from Mono)
	 * "Java Mono Message"
	 */
	@Test
	void testSupplierMonoMessage() {
		webTestClient.get()
			.uri("/supplierMonoMessage")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals("javaMonoMsg", "true")
			.expectBody(String.class)
			.isEqualTo("Java Mono Message");
	}
}
