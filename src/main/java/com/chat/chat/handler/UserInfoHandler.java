package com.chat.chat.handler;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.util.ResponseUtils;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.service.MemberService;
import com.chat.chat.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * (공통)- 토큰 파싱
 * request.attribute(memberId) 꺼내기 ->
 * attribute 항상 Object 타입을 반환 -> cast 로 내가 쓸 타입으로 변환
 * 만약에 null 값이 들어올 수 있으니 justOrEmpty 로 래핑 -> null 이 들어오면 터트려야 함
 *
 * retrievedUserInfo -
 * memberId 만들어오면 바로 service 로 전달
 *
 * editUserInfo-
 * body에 유효한 데이터만 들어오면 service 전달
 */
@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class UserInfoHandler {

    private final UserInfoService userInfoService;

    public Mono<ServerResponse> retrievedUserInfoHandler(ServerRequest request) {
    return Mono.justOrEmpty(request.attribute("memberId"))
            .cast(String.class)
            .doOnNext(memberId->log.info("memberId:{}",memberId))
            .flatMap(memberId->userInfoService.getUserInfo(Mono.just(memberId)))
            .flatMap(memberResponse->ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ResponseUtils.success("User Info retrieved success",
                            Map.of("memberId",memberResponse.getMemberId(),
                                    "createdAt",memberResponse.getCreateTime().toString())))

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
            }));




    }
    public Mono<ServerResponse> editUserInfoHandler(ServerRequest request) {
        return null;
    }

    public Mono<ServerResponse> deleteUserInfoHandler(ServerRequest request) {
        return null;
    }
    }


