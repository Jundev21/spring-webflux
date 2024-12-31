package com.chat.chat.dto.response;

import java.time.LocalDate;

import com.chat.chat.entity.Message;
import com.chat.chat.entity.Room;

public record MessageResponse(
	String memberId,
	String content,
	String roomName,
	LocalDate createdDate
) {

	public static MessageResponse messageResponse(Message message) {
		return new MessageResponse(
			message.getMemberSenderId(),
			message.getContent(),
			message.getRoomId(),
			message.getCreatedDate()
		);
	}
}
