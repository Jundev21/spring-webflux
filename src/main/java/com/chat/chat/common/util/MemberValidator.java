package com.chat.chat.common.util;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.MemberRequest;

public class MemberValidator {
    private MemberValidator() {
    }

    public static void validateForLogin(MemberRequest memberRequest) {
        if (!memberRequest.getMemberId().matches("^[a-zA-Z0-9]{5,15}$")) {
            throw new CustomException("Invalid member id");
        }
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }

    }

    public static void validateForEdit(MemberRequest memberRequest) {
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }
            if (!memberRequest.getMemberNewPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                throw new CustomException("Invalid member new password");
            }
        }

    public static void validateForDelete(MemberRequest memberRequest) {
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")||!memberRequest.getMemberPasswordConfirm().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }
        if (!memberRequest.getMemberPassword().equals(memberRequest.getMemberPasswordConfirm())) {
            throw new CustomException("passwords do not match");
        }
    }
    }
