package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;


/**
 * Test class for verifying the Java Consumer examples in [ConsumerExamples].
 * Each bean is exposed at "/{beanName}" by Spring Cloud Function.
 *
 * ## Consumers Tested:
 * 1. **Consumer<T>** - Plain input, no output (side-effect) -> consumerPlain
 * 2. **Consumer<Mono<T>>** - Plain reactive input (Mono), no output -> consumerMono
 * 3. **Consumer<Flux<T>>** - Reactive stream as input, no output -> consumerFlux
 * 4. **Consumer<Message<String>>** - Message input -> consumerMessage
 * 5. **Consumer<Flux<Message<String>>>** - Flux Message input -> consumerFluxMessage
 */
@FunctionalSpringBootTest
@AutoConfigureWebTestClient
public class ConsumerExamplesTest {

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void setup() {
		this.webTestClient = webTestClient.mutate()
			.responseTimeout(Duration.ofSeconds(120))
			.build();
	}

	/**
	 * 1. Consumer<T> -> consumerPlain
	 * Consumer that takes a single String and performs a side-effect.
	 *
	 * --- Input: ---
	 * POST /consumerPlain
	 * Content-Type: text/plain
	 * "Some logging data"
	 *
	 * --- Output: ---
	 * Status: 202 ACCEPTED
	 * (No response body)
	 */
	@Test
	void testConsumerPlain() {
		webTestClient.post()
			.uri("/consumerPlain")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("Some logging data")
			.exchange()
			.expectStatus().isAccepted();
	}

	/**
	 * 2. Consumer<Mono<T>> -> consumerMono
	 * Consumer that takes a Mono<String> and performs a side-effect.
	 *
	 * --- Input: ---
	 * POST /consumerMono
	 * Content-Type: text/plain
	 * "Reactive Input"
	 *
	 * --- Output: ---
	 * Status: 202 ACCEPTED
	 * (No response body)
	 */
	@Test
	void testConsumerMono() {
		webTestClient.post()
			.uri("/consumerMono")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("Reactive Input")
			.exchange()
			.expectStatus().isAccepted();
	}

	/**
	 * 3. Consumer<Flux<T>> -> consumerFlux
	 * Consumer that takes a Flux<String> and performs a side-effect.
	 *
	 * --- Input: ---
	 * POST /consumerFlux
	 * Content-Type: application/json
	 * ["streamed item 1", "streamed item 2"]
	 *
	 * --- Output: ---
	 * Status: 202 ACCEPTED
	 * (No response body)
	 */
	@Test
	void testConsumerFlux() {
		webTestClient.post()
			.uri("/consumerFlux")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(List.of("streamed item 1", "streamed item 2"))
			.exchange()
			.expectStatus().isAccepted();
	}

	/**
	 * 4. Consumer<Message<String>> -> consumerMessage
	 * Consumes a Message<String>, printing payload and headers.
	 *
	 * --- Input: ---
	 * POST /consumerMessage
	 * Content-Type: text/plain
	 * Header: inputHeader=value1
	 * "Consume This"
	 *
	 * --- Output: ---
	 * Status: 202 ACCEPTED
	 * (No response body)
	 */
	@Test
	void testConsumerMessage() {
		webTestClient.post()
			.uri("/consumerMessage")
			.contentType(MediaType.TEXT_PLAIN)
			.header("inputHeader", "value1")
			.bodyValue("Consume This")
			.exchange()
			.expectStatus().isAccepted();
	}

	/**
	 * 5. Consumer<Flux<Message<String>>> -> consumerFluxMessage
	 * Consumes a Flux of Messages, printing payload and headers for each.
	 *
	 * --- Input: ---
	 * POST /consumerFluxMessage
	 * Content-Type: application/json
	 * Header: fluxHeader=value2
	 * ["FluxMsg1", "FluxMsg2"]
	 *
	 * --- Output: ---
	 * Status: 202 ACCEPTED
	 * (No response body)
	 */
	@Test
	void testConsumerFluxMessage() {
		webTestClient.post()
			.uri("/consumerFluxMessage")
			.contentType(MediaType.APPLICATION_JSON) // Send list of payloads
			.header("fluxHeader", "value2") // Header applied to request, function sees it on messages
			.bodyValue(List.of("FluxMsg1", "FluxMsg2"))
			.exchange()
			.expectStatus().isAccepted();
	}
}
