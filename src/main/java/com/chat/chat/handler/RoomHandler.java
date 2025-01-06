package com.chat.chat.handler;

import com.chat.chat.dto.request.RoomSearchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.SuccessTypes;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
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
                .flatMap(roomService::createRooms)
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
        return roomService.joinRoom(request.pathVariable("roomId"), request.pathVariable("memberId"))
                .map(result -> ResponseUtils.success(SuccessTypes.JOIN_ROOMS.successMessage, result))
                .flatMap(response ->
                        ServerResponse.ok()
                                .body(Mono.just(response), ApiResponse.class))
                .onErrorResume(error ->
                        ServerResponse.badRequest()
                                .body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
                                        ApiResponse.class));

    }

    public Mono<ServerResponse> leaveRoomHandler(ServerRequest request) {
        return roomService.leaveRoom(request.pathVariable("roomId"), request.pathVariable("memberId"))
                .map(result -> ResponseUtils.success(SuccessTypes.LEAVE_ROOMS.successMessage, result))
                .flatMap(response ->
                        ServerResponse.ok()
                                .body(Mono.just(response), ApiResponse.class))
                .onErrorResume(error ->
                        ServerResponse.badRequest()
                                .body(Mono.just(ResponseUtils.failNoBody(error.getMessage(), HttpStatus.BAD_REQUEST)),
                                        ApiResponse.class));
    }


    public Mono<ServerResponse> retrievedUserRoomsHandler(ServerRequest request) {

        Mono<String> memberIdInReq = Mono.justOrEmpty(request.attribute("memberId"))
                .cast(String.class)
                .switchIfEmpty(Mono.error(new CustomException(ErrorTypes.NOT_EXIST_MEMBER.errorMessage)))
                .doOnNext(memberId -> log.info("memberId: {}", memberId));

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        RoomSearchRequest requestWithPagination = new RoomSearchRequest(page, size);

        return memberIdInReq.flatMap(memberId ->
                roomService.getUserAllRooms(memberId, requestWithPagination)
                        .flatMap(roomListResponses ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ResponseUtils.success(
                                                SuccessTypes.USER_ROOM_RETRIEVED_SUCCESSFULLY.successMessage,
                                                roomListResponses)))
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
                        }));

    }


    public Mono<ServerResponse> searchRoomsByTitleHandler(ServerRequest request) {
        Mono<String> memberIdInReq = Mono.justOrEmpty(request.attribute("memberId"))
                .cast(String.class)
                .switchIfEmpty(Mono.error(new CustomException(ErrorTypes.NOT_EXIST_MEMBER.errorMessage)))
                .doOnNext(memberId -> log.info("memberId: {}", memberId));

        String title = request.queryParam("title").orElse("");
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        RoomSearchRequest requestWithPagination = new RoomSearchRequest(title,page, size);

        return memberIdInReq.flatMap(memberId ->
                roomService.searchRoomByTitle(memberId, requestWithPagination))
                .flatMap(rooms -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(SuccessTypes.SEARCH_RESULT_ROOM_RETRIEVED_SUCCESSFULLY.successMessage, rooms))
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
                        }));
    }
}

