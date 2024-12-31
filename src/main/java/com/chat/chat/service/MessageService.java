package com.chat.chat.service;

import static com.chat.chat.common.error.ErrorTypes.*;

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
import com.chat.chat.dto.request.MessageRequest;
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

// WebSocketSession 이 사용되는곳
// 새로운 이벤트를 등록하기위한 서비스
// EmitterProcessor stream에 연결되어있는 모든 클라이언트들에게 데이터를 보냄
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<MessageResponse> createMessage(Mono<MessageRequest> messageRequest) {
		return messageRequest
			.flatMap(messagesInfo ->
				isExistRoom(messagesInfo.roomId())
					.flatMap(e ->
						isExistMember(messagesInfo.memberSenderId()))
					.flatMap(e -> messageRepository.save(new Message(messagesInfo))
						.map(MessageResponse::messageResponse))
			);
	}

	public void saveLiveMessage(Mono<MessageRequest> messageRequest) {
		messageRequest
			.flatMap(messagesInfo -> isExistRoom(messagesInfo.roomId())
				.flatMap(e -> isExistMember(messagesInfo.memberSenderId()))
				.doOnNext(e -> log.info(
					messagesInfo.memberSenderId() + "가" + messagesInfo.roomId() + " 방에 메세지 전송 및 저장 하였습니다."))
				.flatMap(e -> messageRepository.save(new Message(messagesInfo))))
			.subscribe();
	}

	public Mono<Page<MessageResponse>> getAllChatMessage(String roomId, String page, String size) {

		Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
		Query pageQuery = new Query(Criteria.where("roomId").is(roomId)).with(pageable);
		Mono<List<Message>> messagesMono = reactiveMongoTemplate.find(pageQuery, Message.class).collectList();
		Mono<Long> totalElements = reactiveMongoTemplate.count(new Query(), Message.class);

		return Mono.zip(messagesMono, totalElements)
			.map(messageTotalTuple -> {
				List<Message> messages = messageTotalTuple.getT1();
				List<MessageResponse> messageResponse =
					messages.stream().map(MessageResponse::messageResponse).toList();
				log.info("모든 메세지가 조회되었습니다.");
				return PageableExecutionUtils.getPage(messageResponse, pageable, messageTotalTuple::getT2);
			});
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
