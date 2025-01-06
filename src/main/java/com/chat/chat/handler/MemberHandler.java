package com.chat.chat.handler;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.util.MemberValidator;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.request.MemberRequest;
import com.chat.chat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberHandler {

    private final MemberService memberService;

    // Todo : memberRequest 객체를 새로 만드는게 나은지 .. 아니면 json inculde 로 사용해도 괜찮은지
    // Todo : AdviceController 생성해서 handler 간단하게 만들기

    public Mono<ServerResponse> createNewMemberHandler(ServerRequest request) {
        return request.bodyToMono(MemberRequest.class)
                .flatMap(memberRequest ->
                        MemberValidator.validateForLogin(memberRequest).then(Mono.just(memberRequest)))
                .doOnNext(memberRequest -> log.info("요청받은 객체 , memberNewPassword , memberPasswordConfirm 이 null 이여야 정상: {}", memberRequest))
                .flatMap(MemberRequest -> memberService.register(MemberRequest)
                .flatMap(member -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(
                                "User register successfully",
                                Map.of("memberId", member.getMemberId())
                        )))

                .onErrorResume(CustomException.class, error -> {
                    log.error("Custom Exception :{}", error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ResponseUtils.fail(error.getMessage()));
                })

                .onErrorResume(error -> {
                    log.error("Unexpected Exception :{}", error.getMessage());
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ResponseUtils.fail("Internal Server Error"));

                }));
    }


    //TODO : 1. handler -> service Mono 로 던지는게 아니라 .. 그 자체 request 로 던질것...
    //TODO : 2. errorhandle => controllerAvice 로 전역적으로 관리하기


    public Mono<ServerResponse> loginMemberHandler(ServerRequest request) {
        return request.bodyToMono(MemberRequest.class)
                .flatMap(memberRequest ->
                        MemberValidator.validateForLogin(memberRequest).then(Mono.just(memberRequest)))
                .doOnNext(memberRequest -> log.info("요청받은 객체 , memberNewPassword , memberPasswordConfirm 이 null 이여야 정상: :{}", memberRequest))
                .flatMap(MemberRequest -> memberService.login(MemberRequest))
                .flatMap(tokenResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(
                                "User login successfully",
                                Map.of(
                                        "memberId", tokenResponse.getMemberId(),
                                        "accessToken", tokenResponse.getAccessToken()
                                )
                        )))
                .onErrorResume(CustomException.class, error -> {
                    log.error("CustomError Exception :{}", error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ResponseUtils.fail(error.getMessage()));
                })
                .onErrorResume(error -> {
                    log.error("Unexpected Exception :{}", error.getMessage());
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ResponseUtils.fail("Internal Server Error"));
                });
    }
}
