package com.chat.chat.router;

import com.chat.chat.handler.MemberHandler;
import com.chat.chat.handler.UserInfoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
@Configuration
public class UserInfoRouter {

// 회원 정보 불러오기 -> 어차피 토큰으로 받음
// 회원 정보 수정하기
// 회원 삭제 하기
    @Bean
    public RouterFunction<ServerResponse> UserInfoRouters(UserInfoHandler userInfoHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/api/user"), userInfoHandler :: retrievedUserInfoHandler)
                .andRoute(RequestPredicates.PATCH("api/user"),userInfoHandler:: editUserInfoHandler)
                .andRoute(RequestPredicates.DELETE("/api/user"),userInfoHandler:: deleteUserInfoHandler);
    }
}
