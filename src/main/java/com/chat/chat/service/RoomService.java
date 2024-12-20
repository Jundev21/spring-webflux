package com.chat.chat.service;

import org.springframework.stereotype.Service;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.RoomResponse;
import com.chat.chat.entity.Room;
import com.chat.chat.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomService {

	private final RoomRepository roomRepository;

	public Mono<RoomResponse> getAllRooms() {

		return null;
	}

	public Mono<RoomResponse> deleteRoom() {

		return null;
	}

	public Mono<Room> createRooms(Mono<RoomRequest> roomRequest) {
		return roomRequest.flatMap(roomData ->
			roomRepository.save(new Room(roomData)));
	}
}
