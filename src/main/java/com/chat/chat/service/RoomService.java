package com.chat.chat.service;

import static com.chat.chat.common.responseEnums.ErrorTypes.*;
import static com.chat.chat.dto.response.RoomListResponse.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.RoomDeleteRequest;
import com.chat.chat.dto.request.RoomRequest;
import com.chat.chat.dto.request.RoomSearchRequest;
import com.chat.chat.dto.response.BasicMemberResponse;
import com.chat.chat.dto.response.BasicRoomResponse;
import com.chat.chat.dto.response.JoinRoomResponse;
import com.chat.chat.dto.response.RoomListResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.entity.Room;
import com.chat.chat.repository.CustomRepository;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RepositorySelector;
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
	private final CustomRepository customRepository;
	private final RepositorySelector repositorySelector;

	public Mono<Page<RoomListResponse>> getAllRooms(String page, String size) {
		Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
		Mono<List<Room>> roomsMono = customRepository.findAllRoomWithPageNation(pageable);
		Mono<Long> totalElements = customRepository.countAllRooms();

		return Mono.zip(roomsMono, totalElements)
			.map(tuple -> {
				List<RoomListResponse> responses = tuple.getT1().stream()
					.map(room -> {
						List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
							.map(BasicMemberResponse::basicMemberResponse).toList();
						return roomListResponse(room, groupMembers);
					}).toList();
				log.info("모든 방이 조회되었습니다.");
				return PageableExecutionUtils.getPage(responses, pageable, tuple::getT2);
			});
	}

	public Mono<JoinRoomResponse> joinRoom(String roomId, String memberId) {
		return isAlreadyJoinedMember(roomId, memberId)
			.then(
				Mono.zip(isExistRoom(roomId), isExistMember(memberId))
					.flatMap(roomMember -> roomRepository.joinRoomMember(roomMember.getT1().getId(),
						roomMember.getT2()))
					.doOnNext(e -> log.info(memberId + " 가 " + roomId + " 에 입장하셨습니다."))
					.flatMap(isUpdated -> roomRepository.findById(roomId))
					.map(room -> new JoinRoomResponse(room.getRoomName(), memberId)));
	}

	public Mono<BasicRoomResponse> deleteRoom(String roomId, RoomDeleteRequest deleteRequest) {
		return isExistRoom(roomId)
			.flatMap(room ->
				checkPassword(room.getRoomPassword(), deleteRequest.password())
					.then(roomRepository.delete(room)
						.doOnNext(e -> log.info("방 " + room.getRoomName() + " 가 삭제되었습니다."))
						.thenReturn(BasicRoomResponse.basicRoomResponse(room)))
			);
	}

	public Mono<RoomListResponse> createRooms(RoomRequest roomRequest, String memberId) {
		return Mono.just(roomRequest)
			.flatMap(roomData ->
				isDuplicatedRoom(roomData.roomName())
					.then(
						isExistMember(memberId)
							.flatMap(memberInfo ->
								roomRepository.save(new Room(roomData, memberInfo))
									.flatMap(savedRoom -> {
										List<BasicMemberResponse> groupMembers = savedRoom.getGroupMembers().stream()
											.map(BasicMemberResponse::basicMemberResponse)
											.toList();
										log.info("방" + savedRoom.getRoomName() + " 이 생성되었습니다.");
										return Mono.just(roomListResponse(savedRoom, groupMembers));
									})
							))
			);
	}

	public Mono<BasicRoomResponse> leaveRoom(String roomId, String userId) {
		return isJoinedMember(roomId, userId)
			.then(
				isExistRoom(roomId)
					.flatMap(e -> roomRepository.removeRoomMember(roomId, userId))
					.doOnNext(room -> log.info(userId + "님이" + roomId + "방에서 나가셨습니다."))
					.flatMap(updatedRoom -> roomRepository.findById(roomId))
					.map(BasicRoomResponse::basicRoomResponse));
	}

	private Mono<Room> isExistRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_ROOM.errorMessage)));
	}

	private Mono<Member> isExistMember(String userId) {
		return memberRepository.findByMemberId(userId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_MEMBER.errorMessage)));
	}

	private Mono<Void> isDuplicatedRoom(String roomName) {
		return roomRepository.existsByRoomName(roomName)
			.flatMap(isFound -> {
				if (isFound) {
					return Mono.error(new CustomException(ALREADY_EXIST_ROOM.errorMessage));
				}
				return Mono.empty();
			});
	}

	public Mono<Void> isJoinedMember(String roomId, String memberId) {
		return customRepository.findJoinedMember(roomId, memberId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_JOINED_MEMBER.errorMessage)))
			.then();
	}

	private Mono<Void> isAlreadyJoinedMember(String roomId, String memberId) {
		return customRepository.findJoinedMember(roomId, memberId)
			.flatMap(member -> Mono.error(new CustomException(ALREADY_JOINED_ROOM.errorMessage)))
			.then();
	}

	private Mono<Void> checkPassword(String originPass, String requestPass) {
		log.info(originPass + " checking password " + requestPass);
		if (!originPass.equals(requestPass)) {
			return Mono.error(new CustomException(NOT_VALID_PASSWORD.errorMessage));
		}
		return Mono.empty();
	}

	public Mono<List<RoomListResponse>> getUserAllRooms(String memberId, RoomSearchRequest searchRequest) {
		return repositorySelector.existInRepo(memberId)
			.then(Mono.defer(() ->
				customRepository.findRoomsByMemberIdWithPagination(memberId, searchRequest.getPage(),
						searchRequest.getSize())
					.doOnNext(room -> log.info("조회된 방: {}", room))
					.map(room -> {
						List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
							.map(BasicMemberResponse::basicMemberResponse)
							.toList();
						return RoomListResponse.roomListResponse(room, groupMembers);
					})
					.doOnNext(response -> log.info("특정 유저의 방 조회: {}", response))
					.collectList()));

    }

	public Mono<List<RoomListResponse>> searchRoomByTitle(String memberId, RoomSearchRequest searchRequest) {
		return repositorySelector.existInRepo(memberId)
			.then(Mono.defer(
				() -> customRepository.findRoomsByTitleWithPagination(searchRequest.getTitle(), searchRequest.getPage(),
						searchRequest.getSize())
					.map(room -> {
						List<BasicMemberResponse> groupMembers = room.getGroupMembers().stream()
							.map(BasicMemberResponse::basicMemberResponse)
							.toList();
						return RoomListResponse.roomListResponse(room, groupMembers);
					})
					.collectList()
			));
	}
}
