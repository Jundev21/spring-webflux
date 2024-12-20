package com.chat.chat.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "message")
@NoArgsConstructor
@Getter
public class Message {
	@Id
	private String id;
	private String content;
	private String memberSenderId;
	private Room chatRoom;
	@CreatedDate
	private LocalDate createdDate;

}
