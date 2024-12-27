package com.chat.chat.dto.response;

import java.util.List;

public record AllMessageResponse(
	List<MessageResponse> messages
) {
	public static AllMessageResponse fromAllMessageResponseDto(List<MessageResponse> message) {

		return new AllMessageResponse(message);

	}
}