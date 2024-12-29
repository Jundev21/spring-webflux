package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class MemberRequest {
    String memberId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberPassword;
}
