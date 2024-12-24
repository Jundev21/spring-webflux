package com.chat.chat.dto.response;

import com.chat.chat.entity.Room;

public record BasicRoomResponse(
	String roomName,
	String adminMemberId
) {

	public static BasicRoomResponse basicRoomResponse(Room room){
		return new BasicRoomResponse(room.getRoomName(), room.getAdminMemberId());
	}
}
