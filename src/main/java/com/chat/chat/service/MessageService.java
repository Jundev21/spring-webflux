package com.chat.chat.service;

import static com.chat.chat.common.responseEnums.ErrorTypes.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.LiveMessageRequest;
import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.response.LiveStreamResponse;
import com.chat.chat.dto.response.MessageResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.entity.Message;
import com.chat.chat.entity.Room;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.MessageRepository;
import com.chat.chat.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<MessageResponse> createMessage(MessageRequest messageRequest, String senderId) {
		return Mono.just(messageRequest)
			.flatMap(messagesInfo -> isExistRoom(messagesInfo.roomId())
				.flatMap(e -> isExistMember(senderId))
				.flatMap(e -> messageRepository.save(new Message(messagesInfo, senderId))
					.map(MessageResponse::messageResponse))
			)
			.doOnNext(e -> log.info(
				messageRequest.roomId() + "방 에서 " + senderId + " 님이 메세지를 생성했습니다."));
	}

	public Mono<LiveStreamResponse> saveLiveMessage(String roomId ,LiveMessageRequest liveMessageRequest, String senderId) {
		return Mono.just(liveMessageRequest)
			.flatMap(messagesInfo -> isExistRoom(roomId)
				.flatMap(e -> isExistMember(senderId))
				.doOnNext(e -> log.info(
					senderId + " 님이 " + roomId + " 방에 메세지 전송 및 저장 하였습니다."))
				.flatMap(e -> messageRepository.save(new Message(roomId, messagesInfo, senderId))))
			.map(LiveStreamResponse::liveStreamResponse);
	}

	public Mono<Page<MessageResponse>> getAllChatMessage(String roomId, String page, String size) {

		Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
		Query pageQuery = new Query(Criteria.where("roomId").is(roomId)).with(pageable);
		Mono<List<Message>> messagesMono = reactiveMongoTemplate.find(pageQuery, Message.class).collectList();
		Mono<Long> totalElements = reactiveMongoTemplate.count(new Query(), Message.class);

		return isExistRoom(roomId)
			.flatMap(passed ->
				Mono.zip(messagesMono, totalElements)
					.map(messageTotalTuple -> {
						List<Message> messages = messageTotalTuple.getT1();
						List<MessageResponse> messageResponse =
							messages.stream().map(MessageResponse::messageResponse).toList();
						log.info(roomId + "의 모든 메세지가 조회되었습니다.");
						return PageableExecutionUtils.getPage(messageResponse, pageable, messageTotalTuple::getT2);
					}));
	}

	public Mono<Room> isExistRoom(String roomId) {
		return roomRepository.findById(roomId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_ROOM.errorMessage)));
	}

	public Mono<Member> isExistMember(String userId) {
		return memberRepository.findByMemberId(userId)
			.switchIfEmpty(Mono.error(new CustomException(NOT_EXIST_MEMBER.errorMessage)));
	}

}
