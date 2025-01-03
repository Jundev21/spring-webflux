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

                .doOnNext(MemberValidator::validateForLogin)
        /**
         * doOnNext 로 하면 Mono<Void> validateFroLogin 결과 값을 받아오지 못함 그 이유는 ?
         *
         * doOnNext 는 사이드 이펙트를 처리하기 위한 연산자임 .. 즉 내부에서 실행된 작업은 결과를 체인으로 전달하지 않음
         * Mono<Void>는 실제로 체인에 영향을 미치지 않고 doOnNext 이후의 스트림에는 여전히 원래의 MemberRequest 가 흐르게 됌
         * validateForLogin 은 Mono<Void> 를 반환하지만 doOnNext 는 반환된 Mono<Void>를 체인에 반영하지 않음
         * 1. doOnNext는 스트림데이터(MemberRequest)를 소비하지만 , 결과적으로 데이터는 변경되지 않음
         * 2. validateForLogin 에서 반환된 Mono<Void>는 doOnNext 내부에서 단순히 무시됌
         */

                .doOnNext(memberRequest -> log.info("Received Request :{}", memberRequest))
                .flatMap(MemberRequest -> memberService.login(Mono.just(MemberRequest)))
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
