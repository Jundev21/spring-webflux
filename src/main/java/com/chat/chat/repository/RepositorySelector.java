package com.chat.chat.repository;


import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.repository.Impl.MemberRepositoryInterface;
import com.chat.chat.repository.redis.RedisMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositorySelector {

    private final RedisMemberRepository redisRepository;
    private final MongoMemberRepository mongoRepository;


    public Mono<MemberRepositoryInterface> selectRepo(String memberId) {
        return redisRepository.existByMemberId(memberId)
                .flatMap(exists -> {
                    if (exists) {
                        log.info("레디스에서 찾음: {}", memberId);
                        return Mono.just(redisRepository);
                    } else {
                        log.info("레디스에서 목찾고, 데이터베이스에서 찾음 {}", memberId);
                        return Mono.just(mongoRepository);
                    }
                })
                .onErrorResume(error -> {
                    log.error("레포지포리 선택 중 예외 발생: {}", memberId);
                    return Mono.error(new CustomException(ErrorTypes.FAILED_TO_SELECT_REPO.errorMessage));
                });
}
    public Mono<Void> existInRepo(String memberId) {
        return redisRepository.existByMemberId(memberId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.empty();
                    } else {
                        return mongoRepository.existByMemberId(memberId)
                                .flatMap(mongoExists -> {
                                    if (mongoExists) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.error(new CustomException(ErrorTypes.NOT_EXIST_MEMBER.errorMessage));
                                    }
                                });
                    }
                });
    }

    public Mono<Void> checkDuplicatedRepo(String memberId) {
        return redisRepository.existByMemberId(memberId)
                .flatMap(existsInRedis -> {
                    if (existsInRedis) {
                        log.error("해당 아이디 중복, Redis에서 찾음: {}", memberId);
                        return Mono.error(new CustomException(ErrorTypes.DUPLICATE_MEMBER_ID.errorMessage));
                    }
                    return mongoRepository.existByMemberId(memberId)
                            .flatMap(existsInMongo -> {
                                if (existsInMongo) {
                                    log.error("해당 아이디 중복, MongoDB에서 찾음: {}", memberId);
                                    return Mono.error(new CustomException(ErrorTypes.DUPLICATE_MEMBER_ID.errorMessage));
                                }
                                log.info("Redis와 MongoDB에서 모두 중복되지 않음: {}", memberId);
                                return Mono.empty();
                            });
                });
    }



}
