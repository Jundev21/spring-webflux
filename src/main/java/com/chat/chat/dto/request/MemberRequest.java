package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 엔드포인트별 필요한 JSON 필드 정리
 *
 * POST - api/auth/register - 회원가입
 * POST - api/auth/login    - 로그인
 * {
 *     "memberId":"",
 *     "memberPassword":""
 * }
 *
 * POST - api/user - 유저 정보 변경하기
 *
 *{
 *     "memberId":"",
 *     "memberPassword":""
 *     "memberNewPassword":""
 * }
 */
@Data
public class MemberRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberId;
    String memberPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberNewPassword;

}

