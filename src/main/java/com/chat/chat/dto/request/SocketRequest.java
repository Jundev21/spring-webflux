package com.chat.chat.dto.request;

public record SocketRequest(
	String memberSenderId,
	String messageContent
) {
}
