package com.chat.chat.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.handler.RoomHandler;

@Configuration
public class RoomRouter {
	@Bean
	public RouterFunction<ServerResponse> roomRouters(RoomHandler roomHandler) {
		return route()
			.path("/api/chat", builder ->
				builder
					.GET("/room", roomHandler::getAllRoomsHandler)
					.POST("/room", roomHandler::createNewRoomHandler)
					.DELETE("/room/{roomId}", roomHandler::deleteRoomHandler)
			)
			.build();

	}
}
