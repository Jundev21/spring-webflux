package com.chat.chat.repository;

import com.chat.chat.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * redis 도
 * repository 처럼 crud 작업을 추상화해서
 * 비지니스 로직 단계에서 직접 redis로직을 수행하지 않고 , 모듈화된 로직으로 접근하는 것이 목표
 * 재사용이 목표
 * <p>
 * 1. 저장 -> Mono
 * => key는 mongo에서 생성되고 가져와야함
 * 2. 찾기 key(id) 값으로  -> Mono
 * 3. 삭제 -> Mono // delete 는 기본적으로 Mono(Long) 반환 .. Boolean으로 바꿔야함
 * 4. key 값 매칭 전부 반환 -> Flux
 * 5. Key(id) 값을 토대로 수정 -> Mono ... redis 는 update 가 없음 ,,set 으로 덮어야
 * 6. exist 여부도 넣는 경우를 봤는데 .. 2번째 기능과 겹칠것 같아서 추상화하지 않음
 * 이라고 생각했지만 만약에 수정할 경우에 존재여부를 확인해야할거같다
 */


@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;


    public Mono<Boolean> save(String key, String value) {
        return redisTemplate.opsForValue().set(key, value);
    }

    public Mono<String> findByKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key).hasElement();
    }

    public Flux<String> findAllRooms() {
        return redisTemplate.keys("roomName*")
                .flatMap(redisTemplate.opsForValue()::get);
    }


    public Mono<Boolean> update(String key, String newValue) {
        return redisTemplate.opsForValue().set(key, newValue);
    }

    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public Mono<Member> findById(String key) {
        return redisTemplate.opsForValue().get(key)
                .map(value -> {
                    Member member = new Member();
                    member.setMemberId(key.replace("memberId:", ""));
                    member.setCreatedDate(LocalDateTime.parse(value));
                    return member;
                });
    }
}
