package com.chat.chat.common.util;

import com.chat.chat.entity.Member;
import com.chat.chat.repository.redis.RedisMemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Redis 구현시 고려사항
 *  1. in-memory 라서 서버가 꺼지면 안에 데이터가 사라짐
 *     -> 대책으로 서버로드 시 "미리로드"하는 preloading을 사용
 *  2. WebFlux 비동기 = Redis에서도 비동기 사용
 *     -> ReactiveRedisTemplate
 *     -> Mongo에서 가져오는 데이터 여러 개 = Flux
 *  3. 저장할 데이터
 *     Key       ||  Value
 *     memberId  ||  seodonghee     ----  (String)
 *     memberPw  ||  hashpw(memberPw) --- (String)
 *     roomName  ||  실제 방 이름 ---(String)
 *  4. 추가 구현 사항
 *     블랙리스트 토큰 관련
 */
@Component
@RequiredArgsConstructor
public class RedisDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataLoader.class);

    private final RedisMemberRepository redisMemberRepository;
    private final ReactiveMongoTemplate mongoTemplate;


    @Bean
    public ApplicationRunner loadDataToRedisBeforeServerStart() {
        return args -> {
            // Members 데이터를 Redis로 로드
          Flux<Member> members = mongoTemplate.findAll(Member.class);
          members.flatMap(redisMemberRepository::saveMember)

// 레디스 코드 바꿈
//            members.flatMap(member -> redisMemberRepository.saveMember(
//                            member.getMemberId(),
//                            member.getMemberPassword(),
//                            member.getCreatedDate()
//                    ))
                    .doOnNext(success -> logger.info("Member 레디스에 저장 성공: {}", success))
                    .doOnError(error -> logger.error("Member 레디스 저장 실패: {}", error.getMessage()))
                    .subscribe();

            // Rooms 데이터를 Redis로 로드

            logger.info("Redis 데이터 로드 작업 완료.");
        };
    }
}
