package com.chat.chat.service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final MemberRepository memberRepository;
    private final RedisRepository redisRepository;

    /**
     * redis 에 있는지 확인 , 없으면 데이터 베이스 , 데이터 베이스에도 없으면 에러
     * redis 에 user created at 추가하기
     *
     * @param memberId
     * @return
     */
    public Mono<MemberResponse> getUserInfo(Mono<String> memberId) {
        return memberId
                .flatMap(id -> redisRepository.exists(id).flatMap(exists -> {
                    if (exists) {
                        return redisRepository.findById(id).flatMap(memberData -> {
                            MemberResponse response = new MemberResponse();
                            response.setMemberId(memberData.getMemberId());
                            response.setCreateTime(memberData.getCreatedDate());
                            return Mono.just(response);
                        });
                    } else {
                        return memberRepository.findByMemberId(id).flatMap(memberData -> {
                            MemberResponse response = new MemberResponse();
                            response.setMemberId(memberData.getMemberId());
                            response.setCreateTime(memberData.getCreatedDate());
                            return Mono.just(response);
                        }).switchIfEmpty(Mono.error(new CustomException("Member not found")));
                    }
                }));
    }
}
