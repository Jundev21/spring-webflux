package com.chat.chat.dto.response;

import java.util.List;

import com.chat.chat.entity.Room;

public record RoomListResponse(

	String roomName,
	String adminMemberId,
	List<BasicMemberResponse> groupMember
) {
	public static RoomListResponse roomListResponse(Room room, List<BasicMemberResponse> basicMemberResponses) {
		return new RoomListResponse(
			room.getRoomName(),
			room.getAdminMemberId(),
			basicMemberResponses
		);
	}

}