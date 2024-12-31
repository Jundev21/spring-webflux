package com.chat.chat.handler;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.request.RoomSearchRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.AllRoomResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomHandler {

    private final RoomService roomService;


    // pageable
    // https://breezymind.com/spring-boot-reactive-mongodb-basic/
    public Mono<ServerResponse> getAllRoomsHandler(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(roomService.getAllRooms(), AllRoomResponse.class);
    }

    public Mono<ServerResponse> deleteRoomHandler(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(roomService.deleteRoom(extractRoomId(request)), BasicRoomResponse.class);
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

    public Mono<ServerResponse> joinRoomHandler(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(roomService.joinRoom(extractRoomId(request), extractUserId(request)), ResponseCookie.class);
    }

    public Mono<ServerResponse> leaveRoomHandler(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(roomService.leaveRoom(extractRoomId(request), extractUserId(request)), BasicRoomResponse.class);
    }

    private String extractRoomId(ServerRequest request) {
        return request.pathVariable("roomId");
    }

    private String extractUserId(ServerRequest request) {
        return request.pathVariable("memberId");
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

        return Mono.zip(memberIdMono,searchRequestMono)
                .flatMap(tuple->{
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
}
