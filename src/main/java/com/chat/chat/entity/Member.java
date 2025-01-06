package com.chat.chat.entity;

import java.time.LocalDateTime;

import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "member")
@NoArgsConstructor
@Getter
@Setter
public class Member {
	@Id
	private String id;
	private String memberId;
	private String memberPassword;
	@CreatedDate
	private LocalDateTime createdDate;


	public Member(String memberId, String memberPassword) {
		this.memberId = memberId;
		this.memberPassword = memberPassword;
	}
}
