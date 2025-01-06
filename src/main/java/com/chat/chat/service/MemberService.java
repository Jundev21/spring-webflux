package com.chat.chat.service;


import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.common.util.JwtUtil;
import com.chat.chat.dto.request.MemberRequest;
import com.chat.chat.dto.response.TokenResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RepositorySelector;
import com.chat.chat.repository.redis.RedisMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepo;
    private final JwtUtil jwtUtil;
    private final RedisMemberRepository redisRepo;
    private final RepositorySelector repositorySelector;


    public Mono<Member> register(MemberRequest memberRequest) {
        return repositorySelector.checkDuplicatedRepo(memberRequest.getMemberId())
                .then(Mono.just(createMember(memberRequest)))
                .flatMap(member ->
                        memberRepo.save(member)
                                .doOnSuccess(databaseSaveResult ->
                                        log.info("회원가입한 유저 데이터베이스 반영 pk: {}", databaseSaveResult.getId()))
                                .then(
                                        redisRepo.saveMember(member)
                                                .doOnSuccess(redisSaveResult ->
                                                        log.info("회원가입한 유저 레디스에 반영 완료 여부: {}", redisSaveResult.toString()))
                                )
                                .thenReturn(member)
                );
    }


    private Member createMember(MemberRequest memberRequest) {
        Member member = new Member();
        member.setMemberId(memberRequest.getMemberId());
        member.setMemberPassword(hashPassword(memberRequest.getMemberPassword()));
        member.setCreatedDate(LocalDateTime.now());
        return member;
    }





    public static String hashPassword(String memberPassword) {
        return BCrypt.hashpw(memberPassword, BCrypt.gensalt());
    }


    public Mono<TokenResponse> login(MemberRequest memberRequest) {
        return repositorySelector.existInRepo(memberRequest.getMemberId())
                .then(repositorySelector.selectRepo(memberRequest.getMemberId()))
                .flatMap(repo -> repo.findMemberById(memberRequest.getMemberId()))
                .flatMap(member -> {
                    if (BCrypt.checkpw(memberRequest.getMemberPassword(), member.getMemberPassword())) {
                        return createTokenResponse(member);
                    } else {
                        log.error("ID 또는 PW가 일치하지 않습니다. MemberId: {}", memberRequest.getMemberId());
                        return Mono.error(new CustomException(ErrorTypes.ID_OR_PW_DO_NOT_MATCH.errorMessage));
                    }
                });
    }


    private Mono<TokenResponse> createTokenResponse(Member member) {
        return jwtUtil.createToken(member.getMemberId())
                .flatMap(tokenByString -> {
                    log.info("로그인 토큰 생성완료");
                    TokenResponse tokenResponse = new TokenResponse();
                    tokenResponse.setMemberId(member.getMemberId());
                    tokenResponse.setAccessToken(tokenByString);
                    return Mono.just(tokenResponse);
                });

    }


}
