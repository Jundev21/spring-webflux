package com.chat.chat.common.util;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.config.JwtConfig;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    // 토큰 생성
    public Mono<String> generateToken(String memberId) {
        return Mono.fromCallable(() -> Jwts.builder()
                .setSubject(memberId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecret())
                .compact());
    }


    public Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                return Jwts.parser()
                        .setSigningKey(jwtConfig.getSecret())
                        .parseClaimsJws(token)
                        .getBody();
            } catch (ExpiredJwtException e) {
                log.error("JWT expired: {}", e.getMessage());
                throw new CustomException("Token expired");
            } catch (Exception e) {
                log.error("Invalid JWT: {}", e.getMessage());
                throw new CustomException("Invalid token");
            }
        });
    }


    public Mono<String> extractMemberId(String token) {
        return validateToken(token).map(Claims::getSubject);
    }

}
