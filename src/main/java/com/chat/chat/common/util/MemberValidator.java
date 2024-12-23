package com.chat.chat.common.util;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.MemberRequest;

public class MemberValidator {
    private MemberValidator() {
    }

    public static void validate(MemberRequest memberRequest) {
        if (!memberRequest.getMemberId().matches("^[a-zA-Z0-9]{5,15}$")) {
            throw new CustomException("Invalid member id");
        }
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }

    }
}
