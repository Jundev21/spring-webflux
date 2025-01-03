package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class MemberRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberId;
    String memberPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberNewPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberPasswordConfirm;
}
/**
 * 엔드포인트별 필요한 JSON 필드 정리
 *
 * POST - api/auth/register
 * POST - api/auth/login
 * {
 *     "memberId":"",
 *     "memberPassword":""
 * }
 * POST - api/user
 *
 *
 */
