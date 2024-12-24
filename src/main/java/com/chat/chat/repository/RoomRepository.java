package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Room;

import reactor.core.publisher.Mono;

@Repository
public interface RoomRepository extends ReactiveMongoRepository<Room,String> {
	@Query("{ '_id' : ?0 }")
	@Update("{ '$pull': { 'groupMembers': { 'memberId': ?1 } } }")
	Mono<Long> removeRoomMember(String roomId, String memberId);

	@Query("{ '_id' : ?0 }")
	@Update("{ '$push': { 'groupMembers': { 'memberId': ?1 } } }")
	Mono<Long> joinRoomMember(String roomId, String memberId);
}
