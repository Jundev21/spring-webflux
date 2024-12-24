package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Member;
import reactor.core.publisher.Mono;

@Repository
public interface MemberRepository extends ReactiveMongoRepository<Member,String> {
    Mono<Member> findByMemberId(String MemberId);
}
