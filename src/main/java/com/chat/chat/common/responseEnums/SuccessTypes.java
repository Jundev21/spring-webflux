package com.chat.chat.common.responseEnums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SuccessTypes {

	DELETE_ROOMS("방 삭제 성공하였습니다. "),
	CREATE_ROOMS("방 생성 성공하였습니다."),
	CREATE_MESSAGES("메세지 생성 성공하였습니다."),
	JOIN_ROOMS("방 입장에 성공하였습니다."),
	LEAVE_ROOMS("방 나가기에 성공하였습니다."),
	GET_ALL_ROOMS("방 조회 성공하였습니다."),
	GET_ALL_Messages("방 전체 메시지 조회 성공하였습니다.");
	public final String successMessage;
}
