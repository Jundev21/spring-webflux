package com.chat.chat.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

	// 웹소켓은 webSocketHandler 를 통해서 관리된다. 클라이언트의 요청이 오면 웹소켓 핸들러는 websocket session 제공한다.
	// websocket session 은 receive 메서드와 send 메서드가있는데 receive 메서드는 클라이언트와 연결된 메세지를 전달받고.
	// send 메서드는 연결된 모든 클라이언트들에게 메세지들 전달한다.

	// spring 한테 어떤 uri 에 웹소켓 통신을 할 것인지 알려주는것
	// path 가 /websocket 이면 localhost:8080/websocket 으로 통신됨
	@Bean
	public HandlerMapping handlerMapping(WebSocketHandler webSocketHandler) {
		Map<String, WebSocketHandler> map = Map.of("/realTimeChat/{roomId}", webSocketHandler);
		return new SimpleUrlHandlerMapping(map, 1);
	}

	// webflux 에서 websocket 을 사용 할 거라고 알려주는 역할.
	@Bean
	public HandlerAdapter wsHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

	@Bean
	public Map<String, Sinks.Many<String>> roomSessionMap() {
		return new HashMap<>();
	}
}
