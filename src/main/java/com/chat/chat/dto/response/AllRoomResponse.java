package com.chat.chat.dto.response;

import java.util.List;

public record AllRoomResponse(
	List<RoomListResponse> rooms
) {
	public static AllRoomResponse fromAllRoomResponseDto(List<RoomListResponse> room) {

		return new AllRoomResponse(room);
	}
}
