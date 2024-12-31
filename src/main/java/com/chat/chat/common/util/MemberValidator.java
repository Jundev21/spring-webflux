package com.chat.chat.common.util;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.request.MemberRequest;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberValidator {

    private final RedisRepository redisRepo;

    private final MemberRepository memberRepo;

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
        if (!memberRequest.getMemberPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$") || !memberRequest.getMemberPasswordConfirm().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new CustomException("Invalid member password");
        }
        if (!memberRequest.getMemberPassword().equals(memberRequest.getMemberPasswordConfirm())) {
            throw new CustomException("passwords do not match");
        }
    }


}
