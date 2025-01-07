package com.chat.chat.handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.common.responseEnums.SuccessTypes;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.response.ApiResponse;
import com.chat.chat.service.MessageService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class MessageHandler {

	private final MessageService messageService;

	public Mono<ServerResponse> getAllChatMessage(ServerRequest request) {
		return messageService.getAllChatMessage(
				extractRoomId(request),
				request.queryParam("page").orElse("0"),
				request.queryParam("size").orElse("10"))
			.map(result -> ResponseUtils.success(SuccessTypes.GET_ALL_Messages.successMessage, result))
			.flatMap(result -> ServerResponse.ok()
				.body(Mono.just(result), ApiResponse.class))
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	public Mono<ServerResponse> createMessage(ServerRequest request) {
		return request.bodyToMono(MessageRequest.class)
			.switchIfEmpty(Mono.error(new CustomException(ErrorTypes.EMPTY_REQUEST.errorMessage)))
			.flatMap(messageInfo -> messageService.createMessage(messageInfo,extractMemberInfo(request)))
			.map(result -> ResponseUtils.success(SuccessTypes.CREATE_MESSAGES.successMessage, result))
			.flatMap(result -> ServerResponse.ok()
				.body(Mono.just(result), ApiResponse.class)
			)
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	private String extractRoomId(ServerRequest request) {
		return request.pathVariable("roomId");
	}
	private String extractMemberInfo(ServerRequest request){
		return request.exchange().getAttribute("memberId");
	}

}
