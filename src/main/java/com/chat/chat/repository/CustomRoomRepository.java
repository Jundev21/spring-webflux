package com.chat.chat.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Member;
import com.chat.chat.entity.Room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomRoomRepository {


	private final ReactiveMongoTemplate reactiveMongoTemplate;


	public Mono<List<Room>> findAllRoomWithPageNation(Pageable pageable) {
		Query pageableQuery = new Query().with(pageable);
		return reactiveMongoTemplate.find(pageableQuery, Room.class).collectList();
	}

	public Mono<Long> countAllRooms(){
		return  reactiveMongoTemplate.count(new Query(), Room.class);
	}

	public Mono<Room> findJoinedMember(String roomId, String memberId){
		Query findJoinedMember = new Query(Criteria.where("id").is(roomId)
			.and("groupMembers").elemMatch(Criteria.where("memberId").is(memberId)));
		return  reactiveMongoTemplate.findOne(findJoinedMember, Room.class);
	}

}
