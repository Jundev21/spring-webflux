package com.chat.chat.repository.Impl;

import com.chat.chat.entity.Member;
import reactor.core.publisher.Mono;

public interface MemberRepositoryInterface {

    Mono<Member> findMemberById(String memberId);

    Mono<Boolean> saveMember(Member member);

    Mono<Boolean> existByMemberId(String memberId);


    Mono<Boolean> deleteMember(String memberId);
}
