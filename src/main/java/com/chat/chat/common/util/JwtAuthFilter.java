package com.chat.chat.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * 들어오는 데이터 : a:ServerWebExchange / b:WebFilterChain
 *
 * exchange 객체 포함하는거
 * = 요청 데이터 {http method, Path, header(authorization, content-type),본문(json ,xml)}
 * = 응답 데이터 {초기 상태는 빈상태}
 *
 * 필터1 -> 필터2 -> ..-> 핸들러
 * 다음필터로 넘어갈때 webFilterChain.filter(exchange) 를 호출
 *
 * 필터에 회원가입 , 로그인 엔드 포인트는 제외되야함
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain webFilterChain) {

        //Todo
        return webFilterChain.filter(exchange);
    }
}
