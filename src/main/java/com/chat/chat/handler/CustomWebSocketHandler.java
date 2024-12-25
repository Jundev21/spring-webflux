package com.chat.chat.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.chat.chat.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

// websocket handler 를 사용해서 통신 데이터들을 읽어올 수 있다.
@Configuration
@RequiredArgsConstructor
public class CustomWebSocketHandler implements WebSocketHandler {

	private final Sinks.Many<String> sink;
	private final MessageService messageService;
	private final ObjectMapper objectMapper;

	// @Override
	// public Mono<Void> handle(WebSocketSession session) {
	// 	Flux<WebSocketMessage> messages = session.receive()
	// 		.doOnNext(message -> System.out.println("read message" + message.getPayloadAsText()))
	// 		// .flatMap(message -> {
	// 		// 	// or read message here
	// 		// 	return messageService.getMessages();
	// 		// })
	// 		.flatMap(o -> {
	// 			try {
	// 				System.out.println("send message" + o.getPayloadAsText());
	// 				return Mono.just(objectMapper.writeValueAsString(o));
	// 			} catch (JsonProcessingException e) {
	// 				return Mono.error(e);
	// 			}
	// 		}).map(session::textMessage);
	//
	//
	// 	return session.send(messages);
	// }

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		// 클라이언트에서 받은 메세지
		session.receive()
			.doOnNext(message -> {
				System.out.println("받은 메세지 " + message.getPayloadAsText());
				sink.tryEmitNext(message.getPayloadAsText()); // sink 로 데이터 publish
			})
			.subscribe();

		// websocket에 연결되어있는 모든 클라리언트로 메세지 전송
		Flux<WebSocketMessage> sendMessages = sink.asFlux()
			.flatMap(message -> {
				try {
					System.out.println("전송되는 메세지 " + objectMapper.writeValueAsString(message));
					return Mono.just(session.textMessage(objectMapper.writeValueAsString(message)));
				} catch (JsonProcessingException e) {
					return Mono.error(e);
				}
			});

		return session.send(sendMessages);
	}
}
