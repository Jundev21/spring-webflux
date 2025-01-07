package com.chat.chat.common.responseEnums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SuccessTypes {
	// room 관련 enum
	DELETE_ROOMS("방 삭제 성공하였습니다. "),
	CREATE_ROOMS("방 생성 성공하였습니다."),
	JOIN_ROOMS("방 입장에 성공하였습니다."),
	LEAVE_ROOMS("방 나가기에 성공하였습니다."),
	GET_ALL_ROOMS("방 조회 성공하였습니다."),
	SEARCH_RESULT_ROOM_RETRIEVED_SUCCESSFULLY("검색 방 정보 불러오기 성공"),
	// 메세지 관련 enum
	GET_ALL_Messages("방 전체 메시지 조회 성공하였습니다."),
	CREATE_MESSAGES("메세지 생성 성공하였습니다."),
	// user 관련 enum
	USER_REGISTER_SUCCESSFULLY("회원가입에 성공하였습니다"),
	USER_LOGIN_SUCCESSFULLY("로그인에 성공하였습니다"),
	USER_PW_UPDATE_SUCCESSFULLY("비밀번호 업데이트에 성공하였습니다"),
	USER_INFO_RETRIEVED_SUCCESSFULLY("요청하신 유저정보 불러오기를 성공하였습니다"),
	DELETE_SUCCESS("유저 정보 삭제 성공"),
	USER_ROOM_RETRIEVED_SUCCESSFULLY("유저의 방 정보 불러오기 성공");
	public final String successMessage;
}
