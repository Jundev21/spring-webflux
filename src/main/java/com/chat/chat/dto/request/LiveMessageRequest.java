package com.chat.chat.dto.request;

import lombok.Builder;

@Builder
public record LiveMessageRequest(
	String messageContent
) {
	static public LiveMessageRequest liveMessageRequest(SocketRequest socketRequest) {
		return LiveMessageRequest.builder()
			.messageContent(socketRequest.messageContent())
			.build();
	}
}
