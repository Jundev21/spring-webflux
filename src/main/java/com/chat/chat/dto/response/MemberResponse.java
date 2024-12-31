package com.chat.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class MemberResponse {
    String memberId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    LocalDateTime createTime;
}
