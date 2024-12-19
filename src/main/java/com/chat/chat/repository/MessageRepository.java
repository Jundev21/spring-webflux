package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Message;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<Message,String> {
}
