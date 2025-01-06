package com.chat.chat.common.responseEnums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorTypes {
	EMPTY_REQUEST("요청값이 비어있습니다."),
	NOT_EXIST_ROOM("존재하지 않는 방입니다."),
	ALREADY_EXIST_ROOM("이미 존재하는 방입니다."),
	ALREADY_JOINED_ROOM("이미 방에 참여한 멤버입니다."),
	NOT_JOINED_MEMBER("방에 참여하지 않은 멤버입니다."),
	NOT_EXIST_MEMBER("존재하지 않는 멤버입니다."),
	NOT_VALID_PASSWORD("방 비밀번호가 틀립니다."),
	NOT_VALID_MEMBER_PASSWORD("유저 비밀번호가 틀립니다."),
	INVALID_MEMBER_ID("유효하지 않은 아이디 형식입니다."),
	INVALID_MEMBER_PW("유효하지 않은 패스워드 형식입니다."),
	INVALID_FIELD_VALUE_HAS_BEEN_PROVIDED("유효하지 않은 필드 값이 들어왔습니다."),
	ID_OR_PW_DO_NOT_MATCH("아이디 혹은 패스워드가 일치하지 않습니다"),
	DUPLICATE_MEMBER_ID("중복 아이디 입니다"),
	FAILED_TO_SELECT_REPO("레디스 , 데이터베이스를 선택하는데 문제가 있습니다"),
	FAILED_TO_DELETE_REDIS("레디스에서 삭제하는것을 실패했습니다");
	public final String errorMessage;
}
