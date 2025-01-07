package com.chat.chat.common.responseEnums;

import io.jsonwebtoken.Header;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorTypes {
	EMPTY_REQUEST("요청값이 비어있습니다."),
	SOCKET_ERROR("소켓 전송에 에러가 있습니다."),
	SOCKET_READ_REQUEST_ERROR("소켓 데이터 정보에 오류가있습니다."),
	NOT_EXIST_ROOM("존재하지 않는 방입니다."),
	ALREADY_EXIST_ROOM("이미 존재하는 방입니다."),
	ALREADY_JOINED_ROOM("이미 방에 참여한 멤버입니다."),
	NOT_JOINED_MEMBER("방에 참여하지 않은 멤버입니다."),
	NOT_EXIST_MEMBER("존재하지 않는 멤버입니다."),
	NOT_VALID_PASSWORD("방 비밀번호가 틀립니다."),
	NOT_VALID_MEMBER_PASSWORD("유저 비밀번호가 틀립니다."),
	INVALID_MEMBER_ID("유효하지 않은 아이디 형식입니다."),
	INVALID_MEMBER_PW("유효하지 않은 패스워드 형식입니다."),
	CHECK_NEW_MEMBER_PW("새로 입력한 패스워드를 확인해주세요"),
	INVALID_FIELD_VALUE_HAS_BEEN_PROVIDED("유효하지 않은 필드 값이 들어왔습니다."),
	ID_OR_PW_DO_NOT_MATCH("아이디 혹은 패스워드가 일치하지 않습니다"),
	DUPLICATE_MEMBER_ID("중복 아이디 입니다"),
	REDIS_UPDATE_FAILED("레디스에서 업데이트를 실패"),
	REDIS_SAVE_FAILED("레디스에서 저장 실패"),
	REDIS_DELETE_FAILED("레디스에서 삭제 실패"),
	FAILED_TO_SELECT_REPO("레디스 , 데이터베이스를 선택하는데 문제가 있습니다"),
	FAILED_TO_DELETE_REDIS("레디스에서 삭제하는것을 실패했습니다"),
	NOT_EXIST_JWT("토큰이 존재하지 않습니다."),
	HEADER_IS_NOT_VALID("요청의 헤더가 유효하지 않습니다");
	public final String errorMessage;
}
