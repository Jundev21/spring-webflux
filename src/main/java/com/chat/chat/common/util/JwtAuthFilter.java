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
 * 4. memberId 를 클레임에 저장해서 다른 엔트포인트에서 serverRequest 에서 꺼내쓸수 있도록 설계
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.equals("/api/auth/login")||path.equals("/api/auth/register")
//          ||path.equals("/api/chat/room")
        ) {

            return chain.filter(exchange);
        }
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header!=null || !header.startsWith("Bearer ")) {
            Mono.error(new CustomException("header is not valid"));
        }

        String token = header.substring(7);
        return jwtUtil.validateToken(token)
                .flatMap(claims -> {
                    String memberId = claims.getSubject();
                    exchange.getAttributes().put("memberId", memberId);
                    System.out.println("JWT_memberId:"+ memberId);
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
