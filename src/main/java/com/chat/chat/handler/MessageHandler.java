package com.chat.chat.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.service.MessageService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class MessageHandler {

	private final MessageService messageService;

	public Mono<ServerResponse> getAllChatMessage(ServerRequest request) {
		return ServerResponse
			.ok()
			.body(messageService.getAllChatMessage(
				extractRoomId(request),
				request.queryParam("page").orElse("0"),
				request.queryParam("size").orElse("10")
			), BasicRoomResponse.class);
	}

	public Mono<ServerResponse> createMessage(ServerRequest request) {
		return request.bodyToMono(MessageRequest.class)
			.switchIfEmpty(Mono.error(new ServerWebInputException("Request body cannot be empty.")))
			.flatMap(messageRequest-> messageService.createMessage(Mono.just(messageRequest)))
			.flatMap(createdMessage ->
				ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue("success")
			);
	}

	private String extractRoomId(ServerRequest request){
		return request.pathVariable("roomId");
	}
}
