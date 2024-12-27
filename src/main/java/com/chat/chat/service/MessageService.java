package com.chat.chat.service;

import static com.chat.chat.dto.response.MessageResponse.*;

import org.springframework.stereotype.Service;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.response.MessageResponse;
import com.chat.chat.entity.Message;
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

	public Mono<MessageResponse> createMessage(Mono<MessageRequest> messageRequest){
		return messageRequest
			.flatMap(messagesInfo-> roomRepository.findById(messagesInfo.roomId())
			   .switchIfEmpty(Mono.error(new IllegalArgumentException("없는 방입니다.")))
			   .flatMap(e-> memberRepository.findByMemberId(messagesInfo.memberSenderId()))
			   .switchIfEmpty(Mono.error(new IllegalArgumentException("없는 사용자")))
				.flatMap(e -> messageRepository.save(new Message(messagesInfo))
					.map(MessageResponse::messageResponse)));
	}

	public void saveLiveMessage(Mono<MessageRequest> messageRequest){
		messageRequest
			.flatMap(messagesInfo -> roomRepository.findById(messagesInfo.roomId())
				.switchIfEmpty(Mono.error(new IllegalArgumentException("없는 방입니다.")))
				.flatMap(e -> memberRepository.findByMemberId(messagesInfo.memberSenderId()))
				.switchIfEmpty(Mono.error(new IllegalArgumentException(messagesInfo.memberSenderId() + " 는 없는 사용자 입니다.")))
				.doOnNext(e-> log.info(messagesInfo.memberSenderId() + "사용자가" + messagesInfo.roomId() + " 방에 메세지 전송 및 저장 하였습니다."))
				.flatMap(e -> messageRepository.save(new Message(messagesInfo))))
			.subscribe();
	}

	public Mono<MessageResponse> getAllChatMessage(String roomId) {
		return messageRepository.findAllByRoomId(roomId)
			.zipWith(roomRepository.findById(roomId))
			.map(tuple->messageResponse(tuple.getT1(),tuple.getT2()));
	}

}
