package com.chat.chat.dto.response;

import java.time.LocalDateTime;

import com.chat.chat.entity.Message;

import lombok.Builder;

@Builder
public record LiveStreamResponse(
	String messageContent,
	String roomId,
	String memberSenderId,
	LocalDateTime sendDate
) {
	static public LiveStreamResponse liveStreamResponse(Message message) {
		return LiveStreamResponse.builder()
			.messageContent(message.getContent())
			.roomId(message.getRoomId())
			.memberSenderId(message.getMemberSenderId())
			.sendDate(message.getLocalDateTime())
			.build();
	}
}
