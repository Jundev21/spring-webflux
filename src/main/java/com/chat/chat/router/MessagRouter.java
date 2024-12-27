package com.chat.chat.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.handler.MessageHandler;

@Configuration
public class MessagRouter {
	@Bean
	public RouterFunction<ServerResponse> messageRouters(MessageHandler messageHandler) {
		return route()
			.path("/api/chat/message", builder ->
				builder
					.GET("/room/{roomId}", messageHandler::getAllChatMessage)
					.POST("", messageHandler::createMessage)
			).build();

	}
}
