package com.chat.chat.dto.response;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
	String message,
	HttpStatus status
) {

}
