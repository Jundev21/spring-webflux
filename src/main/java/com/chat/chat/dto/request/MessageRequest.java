package com.chat.chat.dto.request;

public record MessageRequest(
	String roomId,
	String messageContent
) {
}
