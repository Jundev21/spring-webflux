package com.chat.chat.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.chat.chat.dto.request.LiveMessageRequest;
import com.chat.chat.dto.request.MessageRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "message")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Data
@Builder
public class Message {
	@Id
	private String id;
	private String content;
	private String memberSenderId;
	private String roomId;
	@CreatedDate
	private LocalDateTime localDateTime;

	public Message(MessageRequest messageRequest, String senderId) {
		this.roomId = messageRequest.roomId();
		this.memberSenderId = senderId;
		this.content = messageRequest.messageContent();
		this.localDateTime = LocalDateTime.now();
	}

	public Message(String roomId, LiveMessageRequest liveMessageRequest, String senderId) {
		this.roomId = roomId;
		this.memberSenderId = senderId;
		this.content = liveMessageRequest.messageContent();
		this.localDateTime = LocalDateTime.now();
	}
}
