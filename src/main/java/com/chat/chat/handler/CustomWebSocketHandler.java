package com.chat.chat.handler;

import java.net.URI;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.service.MessageService;
import com.chat.chat.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.lang.NonNullApi;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

// websocket handler 를 사용해서 통신 데이터들을 읽어올 수 있다.
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
							MessageRequest messageRequest = objectMapper.readValue(message.getPayloadAsText(), MessageRequest.class);
							log.info("Message in room " + room.getRoomName()+ ": " + messageRequest);

						} catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}



						roomSink.tryEmitNext(message.getPayloadAsText());







					})
					.doFinally(signalType -> {

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
		UriTemplate template = new UriTemplate("/notice/{roomId}");
		Map<String, String> parameters = template.match(path);
		return parameters.get("roomId");
	}

	// 	@Override
	// 	public Mono<Void> handle(WebSocketSession session) {
	//
	// 		// 클라이언트에서 받은 메세지 sink 에 대기
	// 		receivedMessage(session);
	// 		// sink 에 대기중인 데이터를 websocket에 연결되어있는 모든 클라리언트로 메세지 전송
	// 		return session.send(sendMessage(session));
	// 	}
	//
	// 	public void receivedMessage(WebSocketSession session) {
	//
	// 		URI uri = session.getHandshakeInfo().getUri();
	// 		UriTemplate template = new UriTemplate("/notice/{roomId}");
	// 		Map<String, String> parameters = template.match(uri.getPath());
	// 		String currentRoomUri = parameters.get("roomId");
	//
	// 		System.out.println("current roomId" + currentRoomUri);
	//
	// 		session.receive()
	// 			.doOnNext(message -> {
	// 				String payload = message.getPayloadAsText();
	// 				// JSON 파싱
	// 				ObjectMapper objectMapper = new ObjectMapper();
	// 				try {
	// 					JsonNode jsonNode = objectMapper.readTree(payload);
	//
	// 					String userId = jsonNode.get("userId").asText();
	// 					String roomId = jsonNode.get("roomId").asText();
	// 					String messageText = jsonNode.get("message").asText();
	//
	// 					System.out.println("사용자 ID: " + userId);
	// 					System.out.println("방 ID: " + roomId);
	// 					System.out.println("메시지 내용: " + messageText);
	//
	// 					sink.tryEmitNext(messageText); // 메시지 내용만 sink로 퍼블리시
	// 				} catch (Exception e) {
	// 					System.err.println("메시지 파싱 실패: " + e.getMessage());
	// 				}
	// 			})
	// 			.subscribe();
	//
	// }
	//
	// public Flux<WebSocketMessage> sendMessage(WebSocketSession session) {
	// 	return sink.asFlux()
	// 		.flatMap(message -> {
	// 			try {
	// 				System.out.println("전송되는 메세지 " + objectMapper.writeValueAsString(message));
	// 				return Mono.just(session.textMessage(objectMapper.writeValueAsString(message)));
	// 			} catch (JsonProcessingException e) {
	// 				return Mono.error(e);
	// 			}
	// 		});
	// }

	//========================================================================================================================
	// private final Map<String, Sinks.Many<String>> roomSinkMap;
	// private final MessageService messageService;
	// private final ObjectMapper objectMapper;
	// @Override
	// public Mono<Void> handle(WebSocketSession session) {
	// 	// roomId 추출
	// 	String path = session.getHandshakeInfo().getUri().getPath();
	// 	UriTemplate template = new UriTemplate("/notice/{roomId}");
	// 	Map<String, String> parameters = template.match(path);
	// 	String roomId = parameters.get("roomId");
	//
	// 	// roomId에 대한 sink 가져오기 또는 생성
	// 	roomSinkMap.putIfAbsent(roomId, Sinks.many().multicast().directBestEffort());
	// 	Sinks.Many<String> roomSink = roomSinkMap.get(roomId);
	//
	// 	// 클라이언트에서 메시지 수신 및 처리
	// 	session.receive()
	// 		.doOnNext(message -> {
	// 			String payload = message.getPayloadAsText();
	// 			System.out.println("Received in room " + roomId + ": " + payload);
	// 			roomSink.tryEmitNext(payload); // 해당 방의 sink로 메시지 전송
	// 		})
	// 		.subscribe();
	//
	// 	// 해당 roomId의 메시지를 클라이언트로 전송
	// 	Flux<WebSocketMessage> outputMessages = roomSink.asFlux()
	// 		.map(session::textMessage);
	//
	// 	return session.send(outputMessages);
	// }
	//========================================================================================================================
}
