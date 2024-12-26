package com.chat.chat.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.service.MessageService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class MessageHandler {

	private final MessageService messageService;

	public Mono<ServerResponse> getAllMessage(ServerRequest request) {
		return null;
	}

	public Mono<ServerResponse> sendMessage(ServerRequest request) {
		return null;
	}

	public Mono<ServerResponse> testMessage(ServerRequest request) {

		return ServerResponse.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("success");
	}
}
