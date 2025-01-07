package com.chat.chat.dto.response;

import java.util.List;

import com.chat.chat.entity.Room;

import lombok.Builder;

@Builder
public record RoomListResponse(

	String roomId,
	String roomName,
	String adminMemberId,
	List<BasicMemberResponse> groupMember
) {
	public static RoomListResponse roomListResponse(Room room, List<BasicMemberResponse> basicMemberResponses) {
		return RoomListResponse.builder()
			.roomId(room.getId())
			.roomName(room.getRoomName())
			.adminMemberId(room.getAdminMemberId())
			.groupMember(basicMemberResponses)
			.build();
	}
}