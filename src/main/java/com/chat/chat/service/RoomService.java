package com.chat.chat.service;

import java.rmi.ServerException;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.AllRoomResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.entity.Room;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

	private final RoomRepository roomRepository;
	private final MemberRepository memberRepository;

	public Mono<AllRoomResponse> getAllRooms() {
		System.out.println("start");
		return roomRepository.findAll()
			.doOnNext(room -> System.out.println("Retrieved room: " + room))
			.map(BasicRoomResponse::basicRoomResponse)
			.collectList()
			.map(AllRoomResponse::fromAllRoomResponseDto);
	}

	public Mono<JoinRoomResponse> joinRoom(String roomId, String userId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("존재하지 않는 방입니다. ")))
			.flatMap(e-> roomRepository.joinRoomMember(roomId,userId))
			.doOnNext(e-> log.info(userId + " 가 입장하셨습니다." ))
			.flatMap(updatedRoom-> roomRepository.findById(roomId))
			.doOnNext(e-> System.out.println(e.getGroupMembers().size()+ " 삭제됨"))
			.map(room -> new JoinRoomResponse(room.getRoomName(), userId));
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

	public Mono<BasicRoomResponse> leaveRoom(String roomId, String userId) {
		return
			roomRepository.findById(roomId)
				.switchIfEmpty(Mono.error(new ServerException("비어있는 방입니다.")))
				.flatMap(e-> roomRepository.removeRoomMember(roomId,userId))
			.doOnNext(e-> System.out.println(e+ " 만큰 업데이트"))
			.flatMap(updatedRoom-> roomRepository.findById(roomId))
			.doOnNext(e-> System.out.println(e.getGroupMembers().size()+ " 삭제됨"))
			.map(room -> new BasicRoomResponse(room.getRoomName(), userId));
	}


}
