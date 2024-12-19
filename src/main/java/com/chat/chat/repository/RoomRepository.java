package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Room;

@Repository
public interface RoomRepository extends ReactiveMongoRepository<Room,String> {
}
