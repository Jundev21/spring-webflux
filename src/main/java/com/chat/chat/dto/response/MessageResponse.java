package com.chat.chat.dto.response;

import java.time.LocalDateTime;

import com.chat.chat.entity.Message;

public record MessageResponse(
	String memberId,
	String content,
	String roomName,
	LocalDateTime createdDate
) {

	public static MessageResponse messageResponse(Message message) {
		return new MessageResponse(
			message.getMemberSenderId(),
			message.getContent(),
			message.getRoomId(),
			message.getLocalDateTime()
		);
	}
}
