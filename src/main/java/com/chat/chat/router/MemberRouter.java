package com.chat.chat.router;

import com.chat.chat.handler.MemberHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class MemberRouter {
    @Bean
    public RouterFunction<ServerResponse> memberRouters(MemberHandler memberHandler) {
        return RouterFunctions
                .route(RequestPredicates.POST("/api/auth/register"), memberHandler::createNewMemberHandler)
                .andRoute(RequestPredicates.POST("/api/auth/login"), memberHandler::loginMemberHandler);
    }
}
