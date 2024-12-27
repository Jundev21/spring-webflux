package com.chat.chat.common.util;

import com.chat.chat.entity.Member;
import com.chat.chat.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * redis 구현시 고려사항
 *  1. in-memory 라서 서버가 꺼지면 안에 데이터가 사라짐
 * -> 대책으로 서버로드시 "미리로드"하는 preloading 을 사용
 * <p>
 * 2. webflux 비동기 = redis 에서도 비동기 사용
 * -> ReactiveRedisTemplate 사용
 * -> Mongo 에서 가져오는 데이터 여러개 = flux
 * <p>
 * 3.저장할 데이터
 * Key       ||  Value
 * memberId  ||  seodonghee     ----  (String)
 * memberPw  ||  hashpw(memberPw) --- (String)
 * roomName  ||  실제 방이름 ---(String)
 *
 * 4.추가구현사항
 * 블랙리스트 토큰 관련
 */
@Component
@RequiredArgsConstructor
public class RedisDataLoader {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveMongoTemplate mongoTemplate;

    @Bean
    public ApplicationRunner loadDataToRedisBeforeServerStart() {
        return args -> {
            Flux<Member> members = mongoTemplate.findAll(Member.class);

            members.subscribe(member -> {
                redisTemplate.opsForValue().set("memberId:" + member.getId(), member.getMemberId()).subscribe();
                redisTemplate.opsForValue().set("memberPw:" + member.getId(), member.getMemberPassword()).subscribe();
            });

            Flux<Room> rooms = mongoTemplate.findAll(Room.class);

            rooms.subscribe(room -> {
                redisTemplate.opsForValue().set("roomName:" + room.getId(), room.getRoomName()).subscribe();
            });

            System.out.println("Redis Load Complete!");
        };
    }
}
