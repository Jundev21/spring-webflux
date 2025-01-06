package com.chat.chat.repository.redis;
import com.chat.chat.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
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
public class RedisRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // 저장
    public Mono<Boolean> saveMember(String memberId, String password, LocalDateTime createdAt) {
        return redisTemplate.opsForHash().putAll("member:" + memberId, Map.of(
                "password", password,
                "createdAt", createdAt.toString()
        ));
    }


    public Mono<String> findPasswordByMemberId(String memberId) {
        return redisTemplate.opsForHash()
                .get("member:" + memberId, "password")
                .map(Object::toString);
    }


    public Mono<LocalDateTime> findCreatedAtByMemberId(String memberId) {
        return redisTemplate.opsForHash()
                .get("member:" + memberId, "createdAt")
                .map(value -> LocalDateTime.parse(value.toString()));
    }


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


    public Mono<Boolean> deleteMember(String memberId) {
        return redisTemplate.delete("member:" + memberId).map(count -> count > 0);
    }

    public Mono<Boolean> updateField(String memberId, String field, String newValue) {
        return redisTemplate.opsForHash()
                .put("member:" + memberId, field, newValue);
    }

    public Mono<Boolean> exists(String memberId) {
        return redisTemplate.hasKey("member:" + memberId);
    }
}