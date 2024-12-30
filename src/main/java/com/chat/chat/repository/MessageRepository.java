package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Message;

import reactor.core.publisher.Mono;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<Message, String> {

	Mono<Message> findAllByRoomId(String roomId);



}
