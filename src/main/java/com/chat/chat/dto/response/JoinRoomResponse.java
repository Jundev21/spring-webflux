package com.chat.chat.dto.response;

import java.util.List;

import com.chat.chat.entity.Member;
import com.chat.chat.entity.Message;

public record JoinRoomResponse(
	String roomName,
	String memberId
) {
}