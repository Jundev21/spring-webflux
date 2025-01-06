package com.chat.chat.dto.request;

import lombok.Builder;

@Builder
public record MessageRequest(
	String roomId,
	String memberSenderId,
	String messageContent
) {

	static public MessageRequest messageRequest(String roomId, SocketRequest socketRequest) {
		return MessageRequest.builder()
			.roomId(roomId)
			.memberSenderId(socketRequest.memberSenderId())
			.messageContent(socketRequest.messageContent())
			.build();
	}
}
