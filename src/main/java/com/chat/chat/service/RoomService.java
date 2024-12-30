package com.chat.chat.service;

import static com.chat.chat.common.error.ErrorTypes.*;
import static com.chat.chat.dto.response.RoomListResponse.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.BasicMemberResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.dto.response.RoomListResponse;
import com.chat.chat.entity.Member;
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
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	//reactive mongodb pagenation 정보
	//https://www.devkuma.com/docs/spring-data-mongo/pagination/
	// https://medium.com/@AADota/spring-mongo-template-pagination-92fa93c50d5c
	//pageable 성능최적화
	//https://junior-datalist.tistory.com/342
	public Mono<Page<RoomListResponse>> getAllRooms(String page, String size) {

		Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
		Query pageableQuery = new Query().with(pageable);

		Mono<List<Room>> roomsMono = reactiveMongoTemplate.find(pageableQuery, Room.class, "room").collectList();
		Mono<Long> countMono = reactiveMongoTemplate.count(pageableQuery, Room.class);

		return Mono.zip(roomsMono, countMono)
			.map(tuple -> {
				List<Room> rooms = tuple.getT1();
				List<RoomListResponse> responses = rooms.stream()
					.map(room -> {
						List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
							.map(BasicMemberResponse::basicMemberResponse)
							.toList();
						return roomListResponse(room, groupMembers);
					}).toList();
				return PageableExecutionUtils.getPage(responses, pageable, tuple::getT2);
			});
	}

	public Mono<JoinRoomResponse> joinRoom(String roomId, String userId) {
		return isExistRoom(roomId)
			.flatMap(e -> roomRepository.joinRoomMember(roomId, userId))
			.doOnNext(e -> log.info(userId + " 가 " + roomId + " 에 입장하셨습니다."))
			.flatMap(updatedRoom -> roomRepository.findById(roomId))
			.map(room -> new JoinRoomResponse(room.getRoomName(), userId));
	}

	public Mono<BasicRoomResponse> deleteRoom(String roomId, RoomDeleteRequest deleteRequest) {
		return
			isExistRoom(roomId)
				.doOnNext(room -> checkPassword(room.getRoomPassword(), deleteRequest.password()))
				.flatMap(room ->
					roomRepository.delete(room)
						.thenReturn(BasicRoomResponse.basicRoomResponse(room)))
				.doOnNext(e -> log.info(e.roomName() + " 가 삭제되었습니다."));

	}

	public Mono<RoomListResponse> createRooms(Mono<RoomRequest> roomRequest) {
		return roomRequest.flatMap(roomData ->
				isExistMember(roomData.adminMemberId())
					.flatMap(memberInfo ->
						roomRepository.save(new Room(roomData, memberInfo))
							.flatMap(savedRoom -> {
								List<BasicMemberResponse> groupMembers = savedRoom.getGroupMembers().stream()
									.map(BasicMemberResponse::basicMemberResponse)
									.toList();
								return Mono.just(roomListResponse(savedRoom, groupMembers));
							})
					)
			)
			.doOnNext(room -> log.info("방" + room.roomName() + " 이 생성되었습니다."));
	}

	public Mono<BasicRoomResponse> leaveRoom(String roomId, String userId) {
		return
			isExistRoom(roomId)
				.flatMap(e -> roomRepository.removeRoomMember(roomId, userId))
				.doOnNext(room -> log.info(userId + "님이" + roomId + "방에서 나가셨습니다."))
				.flatMap(updatedRoom -> roomRepository.findById(roomId))
				.map(BasicRoomResponse::basicRoomResponse);
	}

	public Mono<Room> isExistRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_ROOM.errorMessage)));
	}

	public Mono<Member> isExistMember(String userId) {
		return memberRepository.findById(userId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_MEMBER.errorMessage)));
	}

	private void checkPassword(String originPass, String requestPass) {
		if (!originPass.equals(requestPass)) {
			Mono.error(new CustomException(NOT_VALID_PASSWORD.errorMessage));
		}
	}

}
