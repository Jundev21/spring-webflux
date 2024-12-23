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

    //TODO: 1.HTTP Mappping
    //      2.ControllerAdvice -> if needed
    public Mono<ServerResponse> createNewMemberHandler(ServerRequest request) {
        return request.bodyToMono(MemberRequest.class)
                .doOnNext(MemberValidator ::validate)
                .doOnNext(memberRequest -> log.info("Received request: {}", memberRequest))
                .flatMap(MemberRequest-> memberService.register(Mono.just(MemberRequest)))
                .flatMap(member-> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(
                                "User register successfully",
                                Map.of("memberId", member.getMemberId())
                        )))
                // CustomException 에서 커버 될 경우 에러 처리..
                .onErrorResume(CustomException.class ,error -> {
                    log.error("Custom Exception :{}", error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ResponseUtils.fail(error.getMessage()));
                })
                // TODO:이 부분은 시간이 있다면 .. controllerAdvice 로 빼면 좋을듯..?
                .onErrorResume(error -> {
                log.error("Unexpected Exception :{}", error.getMessage());
                return ServerResponse.status(500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.fail("Internal Server Error"));

    });
    }

}
