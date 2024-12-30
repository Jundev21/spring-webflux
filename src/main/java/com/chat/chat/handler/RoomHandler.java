package com.chat.chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.common.error.ErrorTypes;
import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.dto.response.RoomListResponse;
import com.chat.chat.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomHandler {

	private final RoomService roomService;

	public Mono<ServerResponse> getAllRoomsHandler(ServerRequest request) {
		return ServerResponse.ok()
			.body(roomService.getAllRooms(
					request.queryParam("page").orElse("0"),
					request.queryParam("size").orElse("10"))
				, RoomListResponse.class
			);
	}

	public Mono<ServerResponse> deleteRoomHandler(ServerRequest request) {

		return request.bodyToMono(RoomDeleteRequest.class)
			.switchIfEmpty(Mono.error(new CustomException(ErrorTypes.EMPTY_REQUEST.errorMessage)))
			.flatMap(roomRequest -> roomService.deleteRoom(request.pathVariable("roomId"), roomRequest))
			.flatMap(deletedRoom -> ServerResponse.ok()
				.body(deletedRoom, BasicRoomResponse.class));
	}

	public Mono<ServerResponse> createNewRoomHandler(ServerRequest request) {
		return request.bodyToMono(RoomRequest.class)
			.switchIfEmpty(Mono.error(new CustomException(ErrorTypes.EMPTY_REQUEST.errorMessage)))
			.flatMap(roomRequest -> roomService.createRooms(Mono.just(roomRequest)))
			.flatMap(createdRoom ->
				ServerResponse.ok().body(createdRoom, RoomListResponse.class));
	}

	public Mono<ServerResponse> joinRoomHandler(ServerRequest request) {
		return ServerResponse.ok().body(
			roomService.joinRoom(request.pathVariable("roomId"), request.pathVariable("memberId")),
			JoinRoomResponse.class
		);
	}

	public Mono<ServerResponse> leaveRoomHandler(ServerRequest request) {
		return ServerResponse
			.ok()
			.body(
				roomService.leaveRoom(request.pathVariable("roomId"), request.pathVariable("memberId")),
				BasicRoomResponse.class
			);
	}

}
