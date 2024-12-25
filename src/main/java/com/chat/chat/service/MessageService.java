package com.chat.chat.service;

import org.springframework.stereotype.Service;

import com.chat.chat.entity.Message;

import jdk.jfr.Event;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

// WebSocketSession 이 사용되는곳
// 새로운 이벤트를 등록하기위한 서비스
// EmitterProcessor stream에 연결되어있는 모든 클라이언트들에게 데이터를 보냄
@Service
@RequiredArgsConstructor
public class MessageService {
	// private final EmitterProcessor<Message> processor = EmitterProcessor.create();
	private final Sinks.Many<Message> sink = Sinks.many().multicast().onBackpressureBuffer();

	public void onNext(Message next) {
		sink.tryEmitNext(next);
	};

	public Flux<Message> getMessages() {
		return sink.asFlux();
	}

	public void sendMessages() {
		Message newMessage = Message.builder()
			.id("1")
			.content("really nice to meet you bro")
			.build();
		onNext(newMessage);
	}


}
