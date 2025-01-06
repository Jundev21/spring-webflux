package com.chat.chat.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.SuccessTypes;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.request.RoomSearchRequest;
import com.chat.chat.dto.response.ApiResponse;
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
		return roomService.getAllRooms(
				request.queryParam("page").orElse("0"),
				request.queryParam("size").orElse("10")
			)
			.map(result -> ResponseUtils.success(SuccessTypes.GET_ALL_ROOMS.successMessage, result))
			.flatMap(response ->
				ServerResponse.ok()
					.body(Mono.just(response), ApiResponse.class))
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	public Mono<ServerResponse> deleteRoomHandler(ServerRequest request) {

		return request.bodyToMono(RoomDeleteRequest.class)
			.switchIfEmpty(Mono.error(new CustomException(ErrorTypes.EMPTY_REQUEST.errorMessage)))
			.flatMap(roomRequest -> roomService.deleteRoom(request.pathVariable("roomId"), roomRequest))
			.flatMap(deletedRoom -> ServerResponse.ok()
				.body(Mono.just(ResponseUtils.success(SuccessTypes.DELETE_ROOMS.successMessage, deletedRoom)), ApiResponse.class))
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	public Mono<ServerResponse> createNewRoomHandler(ServerRequest request) {
		return request.bodyToMono(RoomRequest.class)
			.switchIfEmpty(Mono.error(new CustomException(ErrorTypes.EMPTY_REQUEST.errorMessage)))
			.flatMap(roomInfo-> roomService.createRooms(roomInfo, extractMemberInfo(request)))
			.flatMap(createdRoom ->
				ServerResponse.ok().body(
					Mono.just(ResponseUtils.success(SuccessTypes.CREATE_ROOMS.successMessage, createdRoom)), ApiResponse.class
				)
			).onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	public Mono<ServerResponse> joinRoomHandler(ServerRequest request) {
		return roomService.joinRoom(request.pathVariable("roomId"), extractMemberInfo(request))
			.map(result -> ResponseUtils.success(SuccessTypes.JOIN_ROOMS.successMessage,result))
			.flatMap(response ->
				ServerResponse.ok()
					.body(Mono.just(response), ApiResponse.class))
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));

	}

	public Mono<ServerResponse> leaveRoomHandler(ServerRequest request) {
		return roomService.leaveRoom(request.pathVariable("roomId"), extractMemberInfo(request))
			.map(result -> ResponseUtils.success(SuccessTypes.LEAVE_ROOMS.successMessage, result))
			.flatMap(response ->
				ServerResponse.ok()
					.body(Mono.just(response), ApiResponse.class))
			.onErrorResume(error ->
				ServerResponse.badRequest()
					.body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
						ApiResponse.class));
	}

	/**
	 * * 만약에 null 값이 들어올 수 있으니 justOrEmpty 로 래핑 -> null 이 들어오면 터트려야 함 -> 추후 리팩토링 대상
	 * // 성공시 응답값도 리팩토링 필요 현재 위의 로직과 맞추기 위해 ResponseUtils 사용안함
	 */
	public Mono<ServerResponse> retrievedUserRoomsHandler(ServerRequest request) {
		return Mono.justOrEmpty(request.attribute("memberId")).cast(String.class)
			.doOnNext(memberId -> log.info("memberId:{}", memberId))
			.flatMap(memberId -> roomService.getUserAllRooms(memberId))
			.flatMap(userAllRooms -> ServerResponse.ok().
				contentType(MediaType.APPLICATION_JSON).
				bodyValue(userAllRooms))
			.onErrorResume(CustomException.class, error -> {
				log.error("CustomError Exception :{}", error.getMessage());
				return ServerResponse.badRequest()
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(ResponseUtils.fail(error.getMessage()));
			})
			.onErrorResume(error -> {
				log.error("Unexpected Exception :{}", error.getMessage());
				return ServerResponse.status(500)
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(ResponseUtils.fail("Internal Server Error"));
			});
	}

	public Mono<ServerResponse> searchRoomsByTitleHandler(ServerRequest request) {
		Mono<String> memberIdMono = Mono.justOrEmpty(request.attribute("memberId")).cast(String.class)
			.doOnNext(memberId -> log.info("memberId:{}", memberId));

		Mono<RoomSearchRequest> searchRequestMono = request.bodyToMono(RoomSearchRequest.class);

		return Mono.zip(memberIdMono, searchRequestMono)
			.flatMap(tuple -> {
				String memberId = tuple.getT1();
				RoomSearchRequest roomSearchRequest = tuple.getT2();
				return roomService.searchRoomByTitle(
					memberId,
					roomSearchRequest.getTitle(),
					roomSearchRequest.getPage(),
					roomSearchRequest.getSize()
				);
			})
			.flatMap(rooms -> ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(rooms));

	}

	private String extractMemberInfo(ServerRequest request){
		return request.exchange().getAttribute("memberId");
	}
}
