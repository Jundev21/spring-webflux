package com.chat.chat.repository.redis;

import com.chat.chat.entity.Member;
import com.chat.chat.repository.Impl.MemberRepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * redis 도
 * repository 처럼 crud 작업을 추상화해서
 * 비지니스 로직 단계에서 직접 redis로직을 수행하지 않고 , 모듈화된 로직으로 접근하는 것이 목표
 * 재사용이 목표

 */

@Repository
@RequiredArgsConstructor
public class RedisMemberRepository implements MemberRepositoryInterface {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Member> findMemberById(String memberId) {
        return redisTemplate.opsForHash()
                .entries("member:" + memberId)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(fields -> {
                    Member member = new Member();
                    member.setMemberId(memberId);
                    member.setMemberPassword(fields.get("password").toString());
                    member.setCreatedDate(LocalDateTime.parse(fields.get("createdAt").toString()));
                    return member;
                });
    }

    @Override
    public Mono<Boolean> saveMember(Member member) {
        return redisTemplate.opsForHash().putAll("member:" + member.getMemberId(), Map.of(
                "password", member.getMemberPassword(),
                "createdAt", member.getCreatedDate().toString()
        ));
    }

    @Override
    public Mono<Boolean> existByMemberId(String memberId) {
        return redisTemplate.hasKey("member:" + memberId);
    }



    @Override
    public Mono<Boolean> deleteMember(String memberId) {
        return redisTemplate.delete("member:" + memberId).map(count -> count > 0);
    }
}
