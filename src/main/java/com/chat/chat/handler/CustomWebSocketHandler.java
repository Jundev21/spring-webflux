package com.chat.chat.handler;

import static com.chat.chat.common.responseEnums.ErrorTypes.*;

import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.config.JwtConfig;
import com.chat.chat.dto.request.LiveMessageRequest;
import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.request.SocketRequest;
import com.chat.chat.dto.response.LiveStreamResponse;
import com.chat.chat.entity.Room;
import com.chat.chat.service.MessageService;
import com.chat.chat.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CustomWebSocketHandler implements WebSocketHandler {

	private final Map<String, Sinks.Many<String>> roomSinkMap;
	private final MessageService messageService;
	private final RoomService roomService;
	private final ObjectMapper objectMapper;
	private final JwtConfig jwtConfig;

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return Mono.just(extractRoomId(session))
			.flatMap(roomId -> {
				String memberId = extractJwtToken(session);
				return Mono.zip(
					messageService.isExistRoom(roomId),
					roomService.isJoinedMember(roomId, memberId)
						.then(
							Mono.just(memberId)));
			})
			.flatMap(roomInfoMemberId -> {
				// 각 방에있는곳에 메세지 스트림에 구독 hot sequence 임으로 구독 된 후 부터의 메세지 내용들을 전달받음
				roomSinkMap.putIfAbsent(extractRoomId(session), Sinks.many().multicast().onBackpressureBuffer());
				Sinks.Many<String> roomSink = roomSinkMap.get(roomInfoMemberId.getT1().getId());

				// 클라이언트가 전송한 메시지 수신 처리
				session.receive()
					.flatMap(message -> {
						try {
							SocketRequest socketRequest = objectMapper.readValue(message.getPayloadAsText(),
								SocketRequest.class);
							Mono<LiveStreamResponse> liveStreamResponse = messageService.saveLiveMessage(
								roomInfoMemberId.getT1().getId(),
								LiveMessageRequest.liveMessageRequest(socketRequest), roomInfoMemberId.getT2());

							return liveStreamResponse
								.flatMap(response -> {
									try {
										String jsonString = objectMapper.writeValueAsString(response);
										roomSink.tryEmitNext(jsonString);
										return Mono.empty();
									} catch (JsonProcessingException e) {
										return Mono.error(new CustomException(SOCKET_ERROR.errorMessage));
									}
								});
						} catch (JsonProcessingException e) {
							return Mono.error(new CustomException(SOCKET_READ_REQUEST_ERROR.errorMessage));
						}
					})
					.subscribe();
				// Sinks 에 구독되어있는 모든 사용자들에게 메세지 발행
				return session.send(
					roomSink.asFlux()
						.map(session::textMessage)
				);
			});
	}

	private String extractRoomId(WebSocketSession session) {
		String path = session.getHandshakeInfo().getUri().getPath();
		UriTemplate template = new UriTemplate("/realTimeChat/{roomId}");
		Map<String, String> parameters = template.match(path);
		return parameters.get("roomId");
	}

	private String extractJwtToken(WebSocketSession session) {
		String extractToken = Objects.requireNonNull(session.getHandshakeInfo().getHeaders().get("Authorization"),
			NOT_EXIST_JWT.errorMessage).get(0).substring(7);
		return Jwts.parser()
			.setSigningKey(jwtConfig.getSecret())
			.parseClaimsJws(extractToken)
			.getBody().getSubject();

	}

}
