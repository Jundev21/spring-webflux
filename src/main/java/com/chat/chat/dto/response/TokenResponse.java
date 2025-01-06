package com.chat.chat.dto.response;

import lombok.Data;

@Data
public class TokenResponse {
    private String memberId;
    private String accessToken;
}
