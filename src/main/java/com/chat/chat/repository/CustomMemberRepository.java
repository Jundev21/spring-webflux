package com.chat.chat.repository;
import com.chat.chat.entity.Member;
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
}
