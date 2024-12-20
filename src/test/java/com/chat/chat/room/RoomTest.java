package com.chat.chat.room;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.entity.Room;
import com.chat.chat.handler.RoomHandler;
import com.chat.chat.router.RoomRouter;
import com.chat.chat.service.RoomService;

import reactor.core.publisher.Mono;

// 테스트케이스에 어떤 클래그들을 사용할껀지 위한 어노테이션
// WebFluxTest 어노테이션은 라우터와 관련된 클래스들을 감지하지못한다.
@ContextConfiguration(classes = {RoomHandler.class, RoomRouter.class})
@WebFluxTest
public class RoomTest {
	@MockBean
	private RoomService roomService;
	@Autowired
	private WebTestClient webTestClient;
	@Autowired
	private ApplicationContext context;

	@BeforeEach
	public void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(context).build();
	}
	@Test
	@DisplayName("모든 채팅방 조회")
	public void getAllRoomTest() {
		webTestClient.get()
			.uri("/api/chat/room")
			.exchange()
			.expectStatus().isOk();
	}

	@Test
	@DisplayName("채팅방 생성 테스트")
	public void createRoomTest() {
		RoomRequest roomRequest = new RoomRequest("test name", "password", "userAdmin_ID");

		Room savedRoom = new Room(roomRequest);

		when(roomService.createRooms(Mockito.any())).thenReturn(Mono.just(savedRoom));

		webTestClient.post()
			.uri("/api/chat/room")
			.accept(MediaType.APPLICATION_JSON)
			.body(Mono.just(roomRequest), RoomRequest.class)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.roomName").isEqualTo("test name")
			.jsonPath("$.roomPassword").isEqualTo("password")
			.jsonPath("$.adminMemberId").isEqualTo("userAdmin_ID");
	}
}

