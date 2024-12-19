package com.chat.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.chat.entity.Member;

@Repository
public interface MemberRepository extends ReactiveMongoRepository<Member,String> {
}
