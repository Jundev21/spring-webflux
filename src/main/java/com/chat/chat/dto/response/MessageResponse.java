package com.chat.chat.dto.response;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;

public record MessageResponse (
	String memberId,
	String content,
	String roomName,
	LocalDate createdDate
){
}
