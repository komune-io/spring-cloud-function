package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * ## List of Combinations Tested:
 *
 * 1. **Function<T, R>** - Plain input, single output (sync) -> functionPlainToPlain
 * 2. **Function<T, Mono<T>>** - Plain input, single reactive output -> functionPlainToMono
 * 3. **Function<Mono<T>, Mono<R>>** - Mono input, Mono output -> functionMonoToMono
 * 4. **Function<Mono<T>, Flux<R>>** - Mono input, multiple reactive outputs -> functionMonoToFlux
 * 5. **Function<Flux<T>, Mono<R>>** - Reactive stream aggregated to a single result -> functionFluxToMono
 * 6. **Function<Flux<T>, Flux<R>>** - Reactive stream-to-stream transformation -> functionFluxToFlux
 * 7. **Function<T, Flux<R>>** - Plain input, multiple reactive outputs (streamed) -> functionPlainToFlux
 * 8. **Function<Message<String>, Message<Integer>>** - Message input/output -> functionMessageToMessage
 * 9. **Function<Flux<Message<String>>, Flux<Message<Integer>>>** - Flux Message input/output -> functionFluxMessageToFluxMessage
 * 10. **Function<Mono<T>, Mono<R>>** - Mono input, Mono output -> functionMonoToPlain // Modified to be non-blocking
 * 11. **Function<Flux<T>, Mono<R>>** - Flux input, Mono output -> functionFluxToPlain // Modified to be non-blocking
 */
@Configuration
public class FunctionExamples {

	/** 1. Function<T, R> */
	@Bean
	public Function<String, Integer> functionPlainToPlain() {
		return String::length; // Use method reference
	}

	/** 2. Function<T, Mono<T>> */
	@Bean
	public Function<String, Mono<String>> functionPlainToMono() {
		return str -> Mono.just(str.toUpperCase());
	}

	/** 3. Function<Mono<T>, Mono<R>> */
	@Bean
	public Function<Mono<String>, Mono<String>> functionMonoToMono() {
		return mono -> mono.map(String::toUpperCase);
	}

	/** 4. Function<Mono<T>, Flux<R>> */
	@Bean
	public Function<Mono<String>, Flux<String>> functionMonoToFlux() {
		return mono -> mono.flatMapMany(str -> Flux.fromArray(str.split("")));
	}

	/** 5. Function<Flux<T>, Mono<R>> */
	@Bean
	public Function<Flux<String>, Mono<Integer>> functionFluxToMono() {
		// Use count() which returns Mono<Long>
		return flux -> flux.count().map(Long::intValue);
	}

	/** 6. Function<Flux<T>, Flux<R>> */
	@Bean
	public Function<Flux<Integer>, Flux<String>> functionFluxToFlux() {
		return flux -> flux.map(Object::toString);
	}

	/** 7. Function<T, Flux<R>> */
	@Bean
	public Function<String, Flux<String>> functionPlainToFlux() {
		return str -> Flux.fromArray(str.split(""));
	}

	/** 8. Function<Message<String>, Message<Integer>> */
	@Bean
	public Function<Message<String>, Message<Integer>> functionMessageToMessage() {
		return message -> MessageBuilder
			.withPayload(message.getPayload().length())
			.copyHeaders(message.getHeaders())
			.setHeader("javaProcessed", "true")
			.build();
	}

	/** 9. Function<Flux<Message<String>>, Flux<Message<Integer>>> */
	@Bean
	public Function<Flux<Message<String>>, Flux<Message<Integer>>> functionFluxMessageToFluxMessage() {
		return flux -> flux.map(message -> MessageBuilder
			.withPayload(message.getPayload().hashCode())
			.copyHeaders(message.getHeaders())
			.setHeader("javaFluxProcessed", true)
			.build()
		);
	}

 /**
	 * 10. Function<Mono<T>, Integer> // Modified to use a different approach
	 * Takes a Mono<String>, processes the value reactively, returns its length as Integer.
	 * This implementation uses a variable that's updated when the mono emits a value.
	 *
	 * **Example:**
	 * Input: "BlockMono"
	 * Output: 9
	 */
	@Bean
	public Function<Mono<String>, Integer> functionMonoToPlain() {
		return mono -> {
			// Create a result variable
			int[] result = new int[1];

			// Subscribe to the mono and update the result when a value is emitted
			mono.doOnNext(value -> result[0] = (value != null) ? value.length() : 0)
				.doOnSuccess(value -> System.out.println("Processed string with length: " + result[0]))
				.subscribe();

			// Return the result value
			return result[0];
		};
	}

 /**
  * 11. Function<Flux<T>, Integer> // Modified to use a different approach
  * Takes a Flux<String>, counts all values reactively, returns the count as Integer.
  * This implementation uses a counter variable that's updated as elements are emitted by the flux.
  *
  * **Example:**
  * Input: ["Block", "Flux"]
  * Output: 2
  */
 @Bean
 public Function<Flux<String>, Integer> functionFluxToPlain() {
 	return flux -> {
 		// Create a counter variable
 		int[] counter = new int[1];

 		// Subscribe to the flux and increment the counter for each element
 		flux.doOnNext(s -> counter[0]++)
 			.doOnComplete(() -> System.out.println("Counted " + counter[0] + " elements"))
 			.subscribe();

 		// Return the counter value
 		return counter[0];
 	};
 }
}
