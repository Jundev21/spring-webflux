package com.chat.chat.service;

import static com.chat.chat.dto.response.AllMessageResponse.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chat.chat.dto.request.MessageRequest;
import com.chat.chat.dto.response.AllMessageResponse;
import com.chat.chat.dto.response.MessageResponse;
import com.chat.chat.entity.Message;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.MessageRepository;
import com.chat.chat.repository.RoomRepository;

import jdk.jfr.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

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

	public void createMessage(MessageRequest messageRequest){
		memberRepository.findById(messageRequest.memberSenderId())
			.switchIfEmpty(Mono.error(new IllegalArgumentException("없는 사용자")))
			.flatMap(member -> roomRepository.findById(messageRequest.roomId()))
			.switchIfEmpty(Mono.error(new IllegalArgumentException("없는 방입니다.")))
			.map(e -> messageRepository.save(new Message(messageRequest)));
	}

	// public Mono<AllMessageResponse> getMessagesByRoomId(String roomId){
	// 	return messageRepository.findAllByRoomId(roomId)  // roomId로 모든 메시지를 조회
	// 		.flatMap(message ->
	// 			roomRepository.findById(message.getId())  // 방 이름 조회
	// 				.flatMap(room ->
	// 					memberRepository.findById(message.getMemberSenderId())  // 사용자 조회
	// 						.map(user -> new MessageResponse(
	// 							user.getMemberId(),        // 사용자 ID
	// 							message.getContent(),       // 메시지 내용
	// 							room.getRoomName(),         // 방 이름
	// 							message.getCreatedDate()    // 생성 날짜
	// 						))
	// 				)
	// 		)
	// 		.collectList()  // Flux<MessageResponse>를 List<MessageResponse>로 변환
	// 		.map(AllMessageResponse::new);
	//
	// }






}
