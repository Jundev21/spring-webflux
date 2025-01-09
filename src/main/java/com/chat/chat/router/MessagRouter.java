package com.chat.chat.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.request.SocketRequest;
import com.chat.chat.dto.response.ErrorResponse;
import com.chat.chat.dto.response.LiveStreamResponse;
import com.chat.chat.dto.response.RoomListResponse;
import com.chat.chat.handler.MessageHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class MessagRouter {
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/api/chat/message/room/{roomId}",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "Message",
				description = "전체 메세지 조회 API - roomID 를 통해서 해당 방 에 있는 모든 메세지 조회",
				summary = "전체 메세지 조회 API",
				operationId = "getMessage",
				parameters = {@Parameter(in = ParameterIn.PATH, name = "roomId", description = "room id")},
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/message",
			method = RequestMethod.POST,
			operation = @Operation(
				tags = "Message",
				operationId = "postMessage",
				requestBody = @RequestBody(
					required = true,
					description = "메세지 생성시 필수사항 - roomId, content",
					content = @Content(
						schema = @Schema(implementation = MessageRequest.class)
					)
				),
				description = "메세지 생성 API - 방 아이디를 통하여 메세지 생성",
				summary = "메세지 생성 API",
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),

		@RouterOperation(
			path = "/realTimeChat/{roomId}",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "WebSocket",
				operationId = "websocket",
				summary = "실시간 채팅 WS",
				description = "실시간 채팅 API - JWT 토큰과 방ID 그리고 메세지를 전달받아서 웹통신. 통신 성공되면 데이터 저장",
				parameters = {
					@Parameter(name = "roomId", in = ParameterIn.PATH, required = true, description = "roomId")
				},
				requestBody = @RequestBody(
					required = true,
					description = "웹소켓 전달시- messageContent 내용 전달",
					content = @Content(
						schema = @Schema(implementation = SocketRequest.class)
					)
				),
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LiveStreamResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		)
	})
	public RouterFunction<ServerResponse> messageRouters(MessageHandler messageHandler) {
		return route()
			.path("/api/chat/message", builder ->
				builder
					.GET("/room/{roomId}", messageHandler::getAllChatMessage)
					.POST("", messageHandler::createMessage)
			).build();

	}
}
