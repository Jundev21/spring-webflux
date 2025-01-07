package com.chat.chat.handler;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.SuccessTypes;
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

/**
 * Pre-Development:
 *
 * createNewMemberHandler/ loginMemberHandler - 유저 회원가입 핸들러 / 유저 회원가입 핸들러
 * 특징 : 필터에 영향을 안받음 지금 상태는 토큰이 없는 상태이므로.
 * 받는값 : request 는 MemberRequest 객체를 통해 받을 건데 그중 필수 값은 MemberValidator 라는 static 함수로 검사예정
 */

/**
 * Takeaways
 * webflux 는 핸들러와 서비스 등 모든 레이어에서 데이터가 오갈때 명시적으로 publisher 타입을 줘야하는줄 알았는데 아니였음 이미 비동기로 움직이고 있기 때문에
 * 각 레이어사이에서의 호출은 자유로웠음
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MemberHandler {

    private final MemberService memberService;

    public Mono<ServerResponse> createNewMemberHandler(ServerRequest request) {
        return request.bodyToMono(MemberRequest.class)
                .flatMap(memberRequest ->
                        MemberValidator.validateForLogin(memberRequest).then(Mono.just(memberRequest)))
                .doOnNext(memberRequest -> log.info("요청받은 객체 , (memberNewPassword은 null 이여야 정상): {}", memberRequest))
                .flatMap(MemberRequest -> memberService.register(MemberRequest)
                .flatMap(member -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(SuccessTypes.USER_REGISTER_SUCCESSFULLY.successMessage,
                                Map.of("memberId", member.getMemberId())
                        )))
                )
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

                });
    }





    public Mono<ServerResponse> loginMemberHandler(ServerRequest request) {
        return request.bodyToMono(MemberRequest.class)
                .flatMap(memberRequest ->
                        MemberValidator.validateForLogin(memberRequest).then(Mono.just(memberRequest)))
                .doOnNext(memberRequest -> log.info("요청받은 객체 , (memberNewPassword은 null 정상): :{}", memberRequest))
                .flatMap(MemberRequest -> memberService.login(MemberRequest))
                .flatMap(tokenResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ResponseUtils.success(SuccessTypes.USER_LOGIN_SUCCESSFULLY.successMessage,
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
