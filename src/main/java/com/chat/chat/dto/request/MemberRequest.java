package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class MemberRequest {
    String memberId;
    String memberPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberNewPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String memberPasswordConfirm;
}
