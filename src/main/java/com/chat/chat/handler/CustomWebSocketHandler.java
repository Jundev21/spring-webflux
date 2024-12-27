package com.chat.chat.handler;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.service.MessageService;
import com.chat.chat.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return Mono.just(extractRoomId(session))
			.flatMap(roomService::isExistRoom)
			.switchIfEmpty(Mono.error(new IllegalArgumentException("방 못찾음: ")))
			.flatMap(room -> {
				// 각 방에있는곳에 메세지 스트림에 구독
				roomSinkMap.putIfAbsent(room.getId(), Sinks.many().multicast().directBestEffort());
				Sinks.Many<String> roomSink = roomSinkMap.get(room.getId());

				// 클라이언트가 전송한 메시지 수신 처리
				session.receive()
					.doOnNext(message -> {
						try {
							MessageRequest messageRequest = objectMapper.readValue(message.getPayloadAsText(),
								MessageRequest.class);
							messageService.saveLiveMessage(Mono.just(messageRequest));
						} catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}
						roomSink.tryEmitNext(message.getPayloadAsText());
					})
					.subscribe();
				// Sinks 에 구독되어있는 모든 사용자들에게 메세지 발행
				return session.send(
					roomSink.asFlux()
						.map(session::textMessage)
				);
			});
	}

	public String extractRoomId(WebSocketSession session) {
		String path = session.getHandshakeInfo().getUri().getPath();
		UriTemplate template = new UriTemplate("/realTimeChat/{roomId}");
		Map<String, String> parameters = template.match(path);
		return parameters.get("roomId");
	}
}
