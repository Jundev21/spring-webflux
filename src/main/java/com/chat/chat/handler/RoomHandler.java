package com.chat.chat.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import com.chat.chat.dto.request.RoomRequest;
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
		return ServerResponse
			.ok()
			.bodyValue("success");
	}

	public Mono<ServerResponse> deleteRoomHandler(ServerRequest request) {
		long roomId = Long.parseLong(request.pathVariable("roomId"));
		return ServerResponse
			.ok()
			.bodyValue(roomService.deleteRoom());
	}

	public Mono<ServerResponse> createNewRoomHandler(ServerRequest request) {
		return request.bodyToMono(RoomRequest.class)
			.switchIfEmpty(Mono.error(new ServerWebInputException("Request body cannot be empty.")))
			.flatMap(roomRequest -> roomService.createRooms(Mono.just(roomRequest)))
			.flatMap(createdRoom ->
				ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(createdRoom)
			);

	}
}
