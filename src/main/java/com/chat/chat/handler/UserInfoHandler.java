package com.chat.chat.handler;

import com.chat.chat.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Service
@RequiredArgsConstructor
public class UserInfoHandler {

    private final UserInfoService userInfoService;


    public Mono<ServerResponse> retrievedUserInfoHandler(ServerRequest request) {


     return null;

    }

    public Mono<ServerResponse> editUserInfoHandler(ServerRequest request) {
        return null;
    }

    public Mono<ServerResponse> deleteUserInfoHandler(ServerRequest request) {
        return null;
    }
    }


