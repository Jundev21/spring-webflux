package com.chat.chat.common.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorTypes {
	EMPTY_REQUEST("요청값이 비어있습니다."),
	NOT_EXIST_ROOM("존재하지 않는 방입니다."),
	NOT_EXIST_MEMBER("존재하지 않는 멤버입니다."),
	NOT_VALID_PASSWORD("방 비밀번호가 틀립니다."),
	INVALID_MEMBER_ID("유효하지 않은 아이디 형식입니다."),
	INVALID_MEMBER_PW("유효하지 않은 패스워드 형식입니다."),
	INVALID_FIELD_VALUE_HAS_BEEN_PROVIDED("유효하지 않은 필드 값이 들어왔습니다."),
	ID_OR_PW_DO_NOT_MATCH("아이디 혹은 패스워드가 일치하지 않습니다"),
	DUPLICATE_MEMBER_ID("중복 아이디 입니다");
	public final String errorMessage;
}
