package com.chat.chat.service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.MemberRequest;
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

    private String hashPassword(String memberPassword) {
        return BCrypt.hashpw(memberPassword, BCrypt.gensalt());
    }

    // 1. 로그인된 유저 id 가 DB 에 있는지             || exception "User Not Exist"
    // 2. plainText -> hashPassword -> match     || exception "ID or Pw Do Not Match."
    public Mono<Member> login(Mono<MemberRequest> memberRequestMono) {
        return memberRequestMono
                .flatMap(memberReq -> existingUserOrNot(memberReq))// 방출값 : MemberRequest
                .doOnNext(existingMember ->
                {log.info("login memberId: {}", existingMember.getMemberId());});
    }


    private Mono<Member> existingUserOrNot(MemberRequest req) {
        return memberRepository.findByMemberId(req.getMemberId())
                .switchIfEmpty(Mono.error(new CustomException("User Not Exist")))
                .flatMap(member -> {
                    if (BCrypt.checkpw(req.getMemberPassword(), member.getMemberPassword())) {
                        return Mono.just(member);
                    } else {
                        return Mono.error(new CustomException("ID or Pw Do Not Match."));
                    }
                });
    }
}







