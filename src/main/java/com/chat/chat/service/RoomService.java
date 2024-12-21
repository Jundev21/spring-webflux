package com.chat.chat.service;

import java.rmi.ServerException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.AllRoomResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.entity.Room;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomService {

	private final RoomRepository roomRepository;
	private final MemberRepository memberRepository;

	public Mono<AllRoomResponse> getAllRooms() {
		System.out.println("start");
		return roomRepository
			.findAll()
			.doOnNext(room -> System.out.println("Retrieved room: " + room))
			.map(BasicRoomResponse::basicRoomResponse)
			.collectList()
			.map(AllRoomResponse::fromAllRoomResponseDto);
	}

	public Mono<BasicRoomResponse> deleteRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("비어있는 방입니다.")))
			.flatMap(room ->
				roomRepository.delete(room)
				.thenReturn(BasicRoomResponse.basicRoomResponse(room)));
	}

	public Mono<Room> createRooms(Mono<RoomRequest> roomRequest) {
		return roomRequest.flatMap(roomData ->
			roomRepository.save(new Room(roomData)));
	}

	public Mono<JoinRoomResponse> joinRoom(String roomId, String userId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("비어있는 방입니다.")))
			.flatMap(room ->
				memberRepository.findById(userId)
					.switchIfEmpty(Mono.error(new ServerException("없는 사용자입니다.")))
					.map(room::addMember))
			.flatMap(roomRepository::save)
			.map(updatedRoom -> new JoinRoomResponse(updatedRoom.getRoomName(), userId));

	}
}
