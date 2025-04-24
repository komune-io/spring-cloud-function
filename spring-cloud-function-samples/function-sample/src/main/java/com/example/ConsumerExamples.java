package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;


/**
 * ## List of Combinations Tested:
 *
 * 1. **Consumer<T>** - Plain input, no output (side-effect) -> consumerPlain
 * 2. **Consumer<Mono<T>>** - Plain reactive input (Mono), no output -> consumerMono
 * 3. **Consumer<Flux<T>>** - Reactive stream as input, no output -> consumerFlux
 * 4. **Consumer<Message<String>>** - Message input -> consumerMessage
 * 5. **Consumer<Flux<Message<String>>>** - Flux Message input -> consumerFluxMessage
 */
@Configuration
public class ConsumerExamples {

	/** 1. Consumer<T> */
	@Bean
	public Consumer<String> consumerPlain() {
		return str -> System.out.println("Received single: " + str);
	}

	/** 2. Consumer<Mono<T>> */
	@Bean
	public Consumer<Mono<String>> consumerMono() {
		// Consumer<T> requires explicit subscription for Flux/Mono inside the lambda
		return mono -> mono.subscribe(value -> System.out.println("Received Mono: " + value));
	}

	/** 3. Consumer<Flux<T>> */
	@Bean
	public Consumer<Flux<String>> consumerFlux() {
		// Consumer<T> requires explicit subscription for Flux/Mono inside the lambda
		return flux -> flux.subscribe(str -> System.out.println("Received stream item: " + str));
	}

	/**
	 * 4. Consumer<Message<String>>
	 * Consumes a Message<String>, printing payload and headers.
	 */
	@Bean
	public Consumer<Message<String>> consumerMessage() {
		return message -> System.out.println(
			"[Java][Message] Received: " + message.getPayload() + " --- Headers: " + message.getHeaders()
		);
	}

	/**
	 * 5. Consumer<Flux<Message<String>>>
	 * Consumes a Flux of Messages, printing payload and headers for each.
	 */
	@Bean
	public Consumer<Flux<Message<String>>> consumerFluxMessage() {
		// Consumer<T> requires explicit subscription for Flux/Mono inside the lambda
		return flux -> flux.subscribe(message -> System.out.println(
			"[Java][FluxMessage] Received: " + message.getPayload() + " --- Headers: " + message.getHeaders()
		));
	}
}
