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
 * Test class for verifying the Java Function examples in [FunctionExamples].
 * Each bean is exposed at "/{beanName}" by Spring Cloud Function.
 *
 * ## Functions Tested:
 * 1. **Function<T, R>** - Plain input, single output (sync) -> functionPlainToPlain
 * 2. **Function<T, Mono<T>>** - Plain input, single reactive output -> functionPlainToMono
 * 3. **Function<Mono<T>, Mono<R>>** - Mono input, Mono output -> functionMonoToMono
 * 4. **Function<Mono<T>, Flux<R>>** - Mono input, multiple reactive outputs -> functionMonoToFlux
 * 5. **Function<Flux<T>, Mono<R>>** - Reactive stream aggregated to a single result -> functionFluxToMono
 * 6. **Function<Flux<T>, Flux<R>>** - Reactive stream-to-stream transformation -> functionFluxToFlux
 * 7. **Function<T, Flux<R>>** - Plain input, multiple reactive outputs (streamed) -> functionPlainToFlux
 * 8. **Function<Message<String>, Message<Integer>>** - Message input/output -> functionMessageToMessage
 * 9. **Function<Flux<Message<String>>, Flux<Message<Integer>>>** - Flux Message input/output -> functionFluxMessageToFluxMessage
 * 10. **Function<Mono<T>, R>** - Mono input, Plain output (blocking) -> functionMonoToPlain
 * 11. **Function<Flux<T>, R>** - Flux input, Plain output (blocking) -> functionFluxToPlain
 */
@FunctionalSpringBootTest
@AutoConfigureWebTestClient
public class FunctionExamplesTest {

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void setup() {
		this.webTestClient = webTestClient.mutate()
			.responseTimeout(Duration.ofSeconds(120))
			.build();
	}

	/** 1. Function<T, R> -> functionPlainToPlain */
	@Test
	void testFunctionPlainToPlain() {
		webTestClient.post()
			.uri("/functionPlainToPlain")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("Hello")
			.exchange()
			.expectStatus().isOk()
			.expectBody(Integer.class)
			.isEqualTo(5);
	}

	/** 2. Function<T, Mono<T>> -> functionPlainToMono */
	@Test
	void testFunctionPlainToMono() {
		webTestClient.post()
			.uri("/functionPlainToMono")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("hello")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.isEqualTo("HELLO");
	}

	/** 3. Function<Mono<T>, Mono<R>> -> functionMonoToMono */
	@Test
	void testFunctionMonoToMono() {
		webTestClient.post()
			.uri("/functionMonoToMono")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("hello")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.isEqualTo("HELLO");
	}

	/** 4. Function<Mono<T>, Flux<R>> -> functionMonoToFlux */
	@Test
	void testFunctionMonoToFlux() {
		webTestClient.post()
			.uri("/functionMonoToFlux")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("test")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<String>>() {})
			.isEqualTo(List.of("t", "e", "s", "t"));
	}

	/** 5. Function<Flux<T>, Mono<R>> -> functionFluxToMono */
	@Test
	void testFunctionFluxToMono() {
		webTestClient.post()
			.uri("/functionFluxToMono")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(List.of("one", "two", "three"))
			.exchange()
			.expectStatus().isOk()
			// The function returns Mono<Integer>, so expect Integer directly
			.expectBody(Integer.class)
			.isEqualTo(3);
	}

	/** 6. Function<Flux<T>, Flux<R>> -> functionFluxToFlux */
	@Test
	void testFunctionFluxToFlux() {
		webTestClient.post()
			.uri("/functionFluxToFlux")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(List.of(1, 2, 3))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<String>>() {})
			.isEqualTo(List.of("1", "2", "3"));
	}

	/** 7. Function<T, Flux<R>> -> functionPlainToFlux */
	@Test
	void testFunctionPlainToFlux() {
		webTestClient.post()
			.uri("/functionPlainToFlux")
			.contentType(MediaType.TEXT_PLAIN)
			.bodyValue("test")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<String>>() {})
			.isEqualTo(List.of("t", "e", "s", "t"));
	}

	/** 8. Function<Message<String>, Message<Integer>> -> functionMessageToMessage */
	@Test
	void testFunctionMessageToMessage() {
		webTestClient.post()
			.uri("/functionMessageToMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.header("myHeader", "value")
			.bodyValue("Test")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals("javaProcessed", "true")
			.expectHeader().valueEquals("myHeader", "value")
			.expectBody(Integer.class)
			.isEqualTo(4);
	}

	/** 9. Function<Flux<Message<String>>, Flux<Message<Integer>>> -> functionFluxMessageToFluxMessage */
	@Test
	void testFunctionFluxMessageToFluxMessage() {
		webTestClient.post()
			.uri("/functionFluxMessageToFluxMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(List.of("A", "B"))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<Integer>>() {})
			.isEqualTo(List.of("A".hashCode(), "B".hashCode()));
	}

	/**
	 * 10. Function<Mono<T>, Mono<R>> -> functionMonoToPlain
	 * Takes a Mono<String>, maps reactively, returns Mono<Integer> length.
	 * (Framework unwraps Mono to return plain Integer in HTTP response)
	 *
	 * --- Input: ---
	 * POST /functionMonoToPlain
	 * Content-Type: text/plain
	 * "BlockMono"
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * 9
	 */
	@Test
	void testFunctionMonoToPlain() {
		webTestClient.post()
			.uri("/functionMonoToPlain")
			.contentType(MediaType.APPLICATION_JSON) // Send T, framework wraps in Mono<T>
			.bodyValue("BlockMono")
			.exchange()
			.expectStatus().isOk()
			.expectBody(Integer.class) // Expect plain Integer R
			.isEqualTo(9);
	}

	/**
	 * 11. Function<Flux<T>, Mono<R>> -> functionFluxToPlain
	 * Takes a Flux<String>, counts reactively, returns Mono<Integer> count.
	 * (Framework unwraps Mono to return plain Integer in HTTP response)
	 *
	 * --- Input: ---
	 * POST /functionFluxToPlain
	 * Content-Type: application/json
	 * ["Block", "Flux"]
	 *
	 * --- Output: ---
	 * Status: 200 OK
	 * 2
	 */
	@Test
	void testFunctionFluxToPlain() {
		webTestClient.post()
			.uri("/functionFluxToPlain")
			.contentType(MediaType.APPLICATION_JSON) // Send List<T>, framework adapts to Flux<T>
			.bodyValue(List.of("Block", "Flux"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Integer.class) // Expect plain Integer R
			.isEqualTo(2);
	}
}
