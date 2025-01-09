package com.chat.chat.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.ErrorResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.dto.response.RoomListResponse;
import com.chat.chat.handler.RoomHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class RoomRouter {
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/api/chat/room",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "Room",
				operationId = "getAllRooms",
				description = "전체 방 조회 API",
				summary = "전체 방 조회 API",
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/room",
			method = RequestMethod.POST,
			operation = @Operation(
				tags = "Room",
				operationId = "create room",
				description = "방 생성 API - JWT 토큰 사용해서 사용자 방생성",
				summary = "방 생성 API",
				requestBody = @RequestBody(
					required = true,
					description = "방 생성시 방이름, 방 비밀번호 필요",
					content = @Content(
						schema = @Schema(implementation = RoomRequest.class)
					)
				),
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/room/join/{roomId}",
			method = RequestMethod.POST,
			operation = @Operation(
				tags = "Room",
				description = "방 참여 API - JWT 토큰, roomID 를 사용해서 방 생성",
				operationId = "join room",
				summary = "방 참여 API",
				parameters = {@Parameter(in = ParameterIn.PATH, name = "roomId", description = "room id")},
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = JoinRoomResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/room/leave/{roomId}",
			method = RequestMethod.DELETE,
			operation = @Operation(
				tags = "Room",
				description = "방 나가기 API - JWT 토큰, roomID 를 사용해서 방 나가기 ",
				summary = "방 나가기 API",
				parameters = {@Parameter(in = ParameterIn.PATH, name = "roomId", description = "room id")},
				operationId = "leave room",
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BasicRoomResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),

		@RouterOperation(
			path = "/api/chat/room/{roomId}",
			method = RequestMethod.POST,
			operation = @Operation(
				tags = "Room",
				description = "방 삭제 API - JWT 토큰, roomID/password 를 사용해서 방 삭제 ",
				operationId = "delete room",
				summary = "방 삭제 API",
				parameters = {@Parameter(in = ParameterIn.PATH, name = "roomId", description = "room id")},
				requestBody = @RequestBody(
					required = true,
					description = "방 삭제시, 방 비밀번호 필요",
					content = @Content(
						schema = @Schema(implementation = RoomDeleteRequest.class)
					)
				),
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BasicRoomResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/room/users",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "Room",
				description = "사용자가 참여한 방 조회 API - JWT 토큰 사용해서 사용자가 참여한 전체 방 조회",
				summary = "사용자 참여한 전체방 조회 API",
				operationId = "getAllUsersRoom",
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/chat/room/search",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "Room",
				description = "방 검색 API - 방 이름을 검색하여 방을 조회",
				summary = "방 검색 API",
				requestBody = @RequestBody(
					required = true,
					description = "방 검색시 제목, 페이지, 사이즈 필요"
				),
				operationId = "Search room",
				responses = {
					@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RoomListResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		)
	})

	public RouterFunction<ServerResponse> roomRouters(RoomHandler roomHandler) {
		return route()
			.path("/api/chat/room", builder ->
				builder
					.GET("", roomHandler::getAllRoomsHandler)
					.POST("", roomHandler::createNewRoomHandler)
					.POST("/join/{roomId}", roomHandler::joinRoomHandler)
					.DELETE("/leave/{roomId}", roomHandler::leaveRoomHandler)
					.POST("/{roomId}", roomHandler::deleteRoomHandler)
					.GET("/users", roomHandler::retrievedUserRoomsHandler)
					.GET("/search", roomHandler::searchRoomsByTitleHandler)
			)
			.build();

	}
}
