package com.chat.chat.repository;
import com.chat.chat.entity.Member;
import com.chat.chat.entity.Message;
import com.chat.chat.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
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
}
