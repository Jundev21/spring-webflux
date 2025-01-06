package com.chat.chat.repository;

import com.chat.chat.entity.Member;
import com.chat.chat.repository.Impl.MemberRepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class MongoMemberRepository implements MemberRepositoryInterface {

    private final ReactiveMongoRepository<Member, String> memberRepository;

    @Override
    public Mono<Member> findMemberById(String memberId) {
        return memberRepository.findById(memberId);
    }

    @Override
    public Mono<Boolean> saveMember(Member member) {
        return memberRepository.save(member)
                .map(saved -> true)
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> existByMemberId(String memberId) {
        return memberRepository.existsById(memberId);
    }


    @Override
    public Mono<Boolean> deleteMember(String memberId) {
        return memberRepository.deleteById(memberId)
                .then(Mono.just(true))
                .onErrorReturn(false);
    }
}

