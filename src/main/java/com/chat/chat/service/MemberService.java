package com.chat.chat.service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.util.JwtUtil;
import com.chat.chat.dto.request.MemberRequest;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.dto.response.TokenResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public Mono<Member> register(Mono<MemberRequest> memberRequestMono) {
        return memberRequestMono.flatMap(memberRequest ->
                checkDuplicateId(memberRequest.getMemberId())
                        .then(Mono.defer(() -> {
                            //defer -> just
                            Member member = new Member(
                                    memberRequest.getMemberId(),
                                    hashPassword(memberRequest.getMemberPassword()));
                            log.info("Register memberId: {}", memberRequest.getMemberId());
                            return memberRepository.save(member);
                        }))
        );
    }


    private Mono<Void> checkDuplicateId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .flatMap(existingMember -> {
                    log.warn("Duplicate memberId Found: {}", memberId);
                    return Mono.<Void>error(new CustomException("Duplicate memberId"));
                })
                .switchIfEmpty(Mono.empty());

    }

    public static String hashPassword(String memberPassword) {
        return BCrypt.hashpw(memberPassword, BCrypt.gensalt());
    }

    /**
     * 로그인 요청 처리 메서드
     * <p>
     * 1. ID 가 DB에 있는지 확인 / 예외 :"User Not Exist" 반환
     * 2. PW 비교 / 예외 : "ID or Pw Do Not Match" 반환
     * 로그인 성공 시 JWT 토큰 생성
     *
     * @param memberRequestMono {@link Mono} 로그인 데이터
     * @return {@link Mono} 형태로 로그인 처리 결과를 {@link TokenResponse} 반환
     * @throws CustomException 회원이 데이터베이스에 없거나 비밀번호가 일치하지 않을 경우 발생
     */
    public Mono<TokenResponse> login(Mono<MemberRequest> memberRequestMono) {
        return memberRequestMono
                .flatMap(memberReq -> existingUserOrNot(memberReq))
                .doOnNext(existingMember ->
                {
                    log.info("login memberId: {}", existingMember.getMemberId());
                })
                .flatMap(existingUser ->
                        jwtUtil.generateToken(existingUser.getMemberId())
                                .flatMap(token -> {
                                    log.info("login token: {}", token);
                                    log.info("login token: {}", token);
                                    return Mono.just(new TokenResponse(existingUser.getMemberId(), token));
                                }));
    }


    private Mono<Member> existingUserOrNot(MemberRequest req) {
        return memberRepository.findByMemberId(req.getMemberId())
                .switchIfEmpty(
                        Mono.error(new CustomException("User Not Exist")))
                .flatMap(member -> {
                    if (BCrypt.checkpw(req.getMemberPassword(), member.getMemberPassword())) {
                        return Mono.just(member);
                    } else {
                        log.error("ID or Pw Do Not Match");
                        return Mono.error(new CustomException("ID or Pw Do Not Match."));
                    }
                });
    }
}







