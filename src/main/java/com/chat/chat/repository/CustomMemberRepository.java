package com.chat.chat.repository;
import com.chat.chat.entity.Member;
import com.chat.chat.entity.Message;
import com.chat.chat.entity.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomMemberRepository {

    private final ReactiveMongoTemplate mongoTemplate;


    public Mono<Member> updateMemberPassword(String memberId, String newPassword) {
        Query query = Query.query(Criteria.where("memberId").is(memberId));
        Update update = new Update().set("memberPassword", newPassword);

        return mongoTemplate.findAndModify(
                query,
                update,
                Member.class
        );
    }

    public Mono<Message> updateMessageForDeleteUser(String memberId){
        Query query = Query.query(Criteria.where("memberSenderId").is(memberId));
        Update update = new Update().set("memberSenderId", memberId);

        return mongoTemplate.findAndModify(
                query,
                update,
                Message.class
        );
    }

    public Mono<Room> updateRoomForDeleteUser(String memberId){
        Query query = new Query(Criteria.where("groupMembers.memberId").is(memberId));

        Update update = new Update().set("groupMembers.$.memberId", "undefined");

        return mongoTemplate.findAndModify(
                query,
                update,
                Room.class
        );

    }

    public Flux<Room> findRoomsByMemberId(String memberId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("groupMembers.memberId").is(memberId));

        log.info("쿼리 실행: {}", query);
        return mongoTemplate.find(query, Room.class);
    }


    public Flux<Room> findRoomsByTitleWithPagination(String title, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomName").regex(".*" + title  + ".*", "i"));
        query.skip((long) (page - 1) * size);
        query.limit(size);
        log.info("쿼리 로그 : title={}, page={}, size={}, query={}", title, page, size, query);
        return mongoTemplate.find(query, Room.class);
    }

    }



