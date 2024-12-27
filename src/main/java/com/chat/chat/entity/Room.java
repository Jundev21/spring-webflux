package com.chat.chat.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.chat.chat.dto.request.RoomRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "room")
@NoArgsConstructor
@Getter
public class Room {
	@Id
	private String id;
	private String roomName;
	private String roomPassword;
	private String adminMemberId;
	private List<Member> groupMembers = new ArrayList<>();
	@CreatedDate
	private LocalDate createdDate;

	public Room(RoomRequest roomRequest, Member member) {
		this.roomName = roomRequest.roomName();
		this.roomPassword = roomRequest.roomPassword();
		this.adminMemberId = roomRequest.adminMemberId();
		this.groupMembers.add(member);
	}

}
