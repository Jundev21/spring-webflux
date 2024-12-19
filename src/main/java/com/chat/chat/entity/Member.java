package com.chat.chat.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "member")
@NoArgsConstructor
@Getter
public class Member {
	@Id
	private String id;
	private String memberId;
	private String memberPassword;
	@CreatedDate
	private LocalDate createdDate;


}
