package com.chat.chat.common.util;


import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.dto.request.MemberRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Id 조건설정 : 소문자, 대문자, 숫자로만 구성된 5~15자의 문자열
 * Pw 조건설정 : 대문자 최소 1개, 숫자 최소 1개를 포함한 8자 이상의 문자열
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberValidator {

    //TODO : 1. 필요없는 필드 들어왔을 때 에러처리 2.CustomException ErrorType 으로 매핑


    public static Mono<Void> validateForLogin(MemberRequest memberRequest) {
        String memberId = memberRequest.getMemberId();
        String memberPw = memberRequest.getMemberPassword();
        if (!memberId.matches("^[a-zA-Z0-9]{5,15}$")) {
            return  Mono.error(new CustomException(ErrorTypes.INVALID_MEMBER_ID.errorMessage));
        }
        if (!memberPw.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            return Mono.error (new CustomException(ErrorTypes.INVALID_MEMBER_PW.errorMessage));
        }

        if (memberRequest.getMemberNewPassword() != null ||
                memberRequest.getMemberPasswordConfirm() != null) {
            return Mono.error(new CustomException(ErrorTypes.INVALID_FIELD_VALUE_HAS_BEEN_PROVIDED.errorMessage));
        }
        return Mono.empty();
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
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$") || !memberRequest.getMemberPasswordConfirm().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }
        if (!memberRequest.getMemberPassword().equals(memberRequest.getMemberPasswordConfirm())) {
            throw new CustomException("passwords do not match");
        }
    }


}
