package com.chat.chat.common.responseEnums;

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
	NOT_EXIST_JWT("토큰이 존재하지 않습니다."),
	NOT_VALID_PASSWORD("방 비밀번호가 틀립니다.");

	public final String errorMessage;
}
