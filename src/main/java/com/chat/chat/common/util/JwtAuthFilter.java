package com.chat.chat.common.util;

import com.chat.chat.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * a:(ServerWebExchange)
 * = 요청 데이터 {http method, Path, header(authorization, content-type),본문(json ,xml)}
 * = 응답 데이터 {초기 상태는 빈상태}
 *
 * b:(WebFilterChain) 다음 필터 또는 핸들러로 요청을 넘기기 위한 다리
 *
 * 1. 필터에 회원가입 , 로그인 엔드 포인트는 제외되야함
 * 2. header 에서 jwt 추출 - header 적절한지
 * 3. validateToken 호출
 * 4. memberId 를 파싱해서 던지면 받을 곳이 있는지 ...? -> 지금은 당장 쓸곳이 없어서 안던지는걸로
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.equals("/api/auth/login")||path.equals("/api/auth/register")) {
            return chain.filter(exchange);
        }
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header!=null || !header.startsWith("Bearer ")) {
            Mono.error(new CustomException("header is not valid"));
        }

        String token = header.substring(7);
        return jwtUtil.validateToken(token)
                .flatMap(token1 -> chain.filter(exchange))
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    // vaildateToken 에서 배출하는 CusotmError 를 ResponseUtil 에 담에서 에러를 반환해야함
                    return exchange.getResponse().setComplete();
                });

    }
}
