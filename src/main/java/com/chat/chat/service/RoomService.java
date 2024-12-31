package com.chat.chat.service;

import java.rmi.ServerException;
import java.util.List;

import com.chat.chat.repository.CustomMemberRepository;
import org.springframework.stereotype.Service;

import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.response.BasicMemberResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.dto.response.RoomListResponse;
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
	private final CustomMemberRepository customMemberRepository;

	public Mono<List<RoomListResponse>> getAllRooms() {
		return roomRepository.findAll()
			.map(room -> {
				List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
					.map(BasicMemberResponse::basicMemberResponse)
					.toList();
				return RoomListResponse.roomListResponse(room, groupMembers);
			})
			.doOnNext(response -> log.info("전체 방 조회"))
			.collectList();
	}

	public Mono<JoinRoomResponse> joinRoom(String roomId, String userId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("존재하지 않는 방입니다. ")))
			.flatMap(e -> roomRepository.joinRoomMember(roomId, userId))
			.doOnNext(e -> log.info(userId + " 가 입장하셨습니다."))
			.flatMap(updatedRoom -> roomRepository.findById(roomId))
			.doOnNext(e -> System.out.println(e.getGroupMembers().size() + " 삭제됨"))
			.map(room -> new JoinRoomResponse(room.getRoomName(), userId));
	}

	public Mono<BasicRoomResponse> deleteRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("비어있는 방입니다.")))
			.flatMap(room ->
				roomRepository.delete(room)
					.thenReturn(BasicRoomResponse.basicRoomResponse(room)));
	}

	public Mono<RoomListResponse> createRooms(Mono<RoomRequest> roomRequest) {
		return roomRequest.flatMap(roomData ->
			memberRepository.findByMemberId(roomData.adminMemberId())
				.switchIfEmpty(Mono.error(new ServerException("존재하지 않은 멤버입니다.")))
				.flatMap(memberInfo ->
					roomRepository.save(new Room(roomData, memberInfo))
						.flatMap(savedRoom -> {
							List<BasicMemberResponse> groupMembers = savedRoom.getGroupMembers().stream()
								.map(BasicMemberResponse::basicMemberResponse)
								.toList();
							return Mono.just(RoomListResponse.roomListResponse(savedRoom, groupMembers));
						})
				)
		);
	}

	public Mono<BasicRoomResponse> leaveRoom(String roomId, String userId) {
		return
			roomRepository.findById(roomId)
				.switchIfEmpty(Mono.error(new ServerException("비어있는 방입니다.")))
				.flatMap(e -> roomRepository.removeRoomMember(roomId, userId))
				.doOnNext(e -> log.info(roomId + "방 제거됨"))
				.flatMap(updatedRoom -> roomRepository.findById(roomId))
				.doOnNext(e -> System.out.println(e.getGroupMembers().size() + " 삭제됨"))
				.map(BasicRoomResponse::basicRoomResponse);
	}

	public Mono<Room> isExistRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new ServerException("존재하지않는 방입니다.")));
	}

	public Mono<List<RoomListResponse>> getUserAllRooms(String memberId) {
		return customMemberRepository.findRoomsByMemberId(memberId)
				.doOnNext(room -> log.info("조회된 방: {}", room))
				.map(room -> {
					List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
							.map(BasicMemberResponse::basicMemberResponse)
							.toList();
					return RoomListResponse.roomListResponse(room, groupMembers);
				})
				.doOnNext(response -> log.info("특정 유저의 방 조회: {}", response))
				.collectList();
	}

	public Mono<List<RoomListResponse>> searchRoomByTitle(String memberId, String title, int page, int size) {
	// member는 따로 처리 할예정,,,
			return customMemberRepository.findRoomsByTitleWithPagination(title, page, size)
					.map(room -> {
						List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
								.map(BasicMemberResponse::basicMemberResponse)
								.toList();
						return RoomListResponse.roomListResponse(room, groupMembers);
					})
					.collectList();
		}
}
