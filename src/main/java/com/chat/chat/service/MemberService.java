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
                                        log.info("회원가입한 유저 데이터베이스 반영 완료 여부: {}", databaseSaveResult.toString()))
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


    // 해당 checkDuplicatedId 나중에 리팩토링 필요함

//  private Mono<Void> checkDuplicateId(String memberId) {
//        return redisRepo.exists(memberId)
//                .flatMap(existUserInRedis -> {
//                    if (existUserInRedis) {
//                        log.error("해당 아이디 중복, 레디스에서 찾음: {}", memberId);
//                        return Mono.error(new CustomException(ErrorTypes.DUPLICATE_MEMBER_ID.errorMessage));
//                    } else {
//                        return memberRepo.existsByMemberId(memberId)
//                                .flatMap(existUserInDB -> {
//                                    if (existUserInDB) {
//                                        log.error("해당 아이디 중복 데이터베이스에서 찾음: {}", memberId);
//                                        return Mono.error(new CustomException(ErrorTypes.DUPLICATE_MEMBER_ID.errorMessage));
//                                    } else {
//                                        log.info("데이터 베이스 , 레디스 모두에서 해당 아이디가 중복되지 않음: {}", memberId);
//                                        return Mono.empty();
//                                    }
//                                });
//                    }
//
//                });


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


//    public Mono<TokenResponse> login(MemberRequest memberRequestMono) {
//        .flatMap(memberRequestMono.getMemberId()->{
//
//        })
//    }
//        return existingUserOrNot(memberRequestMono) // Mono<Void>
//
//                .doOnNext(existingUser -> {
//                    log.info("로그인 유저는 :{}", existingUser.getMemberId()); // Mono<Member>
//                })
//                .flatMap(member ->
//                        jwtUtil.createToken(member.getMemberId())
//                                .flatMap(tokenByString -> {  // Mono<String> 모노 까서 TokenResponse 에 넣기
//                                    log.info("로그인 토큰 생성완료:{}", tokenByString);
//                                    TokenResponse tokenResponse = new TokenResponse();
//                                    tokenResponse.setMemberId(member.getMemberId());
//                                    tokenResponse.setAccessToken(tokenByString);
//                                    return Mono.just(tokenResponse);
//                                })
//                );
//    }
//
//    private Mono<Void> existingUserOrNot(MemberRequest req) {
//        return redisRepo.exists(req.getMemberId()) // Mono<Boolean>
//                .flatMap(existInRedis->{
//                if (existInRedis) {
//                    return redisRepo.findMemberById(req.getMemberId()) //Mono<Member>
//                            .flatMap(member -> {
//                                if (BCrypt.checkpw(req.getMemberPassword(), member.getMemberPassword())) {
//                                  return Mono.just(member);
//                            } else {
//                                    Mono.error(new CustomException(ErrorTypes.);
//                                }
//                            }
//                })
//                }
//                )
//    }


//        return memberRepo.findByMemberId(req.getMemberId())
//                .switchIfEmpty(
//                        Mono.error(new CustomException(ErrorTypes.NOT_EXIST_MEMBER.errorMessage))
//                )
//                .doOnNext(member -> log.error("유저가 존재하지 않습니다"))
//                .flatMap(member -> {
//                    if (BCrypt.checkpw(req.getMemberPassword(), member.getMemberPassword())) {
//                        return Mono.just(member);
//                    } else {
//                        log.error("ID 또는 Pw 가 일치하지 않습니다");
//                        return Mono.error(new CustomException(ErrorTypes.ID_OR_PW_DO_NOT_MATCH.errorMessage));
//                    }
//                });
//    }



