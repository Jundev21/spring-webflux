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
}



