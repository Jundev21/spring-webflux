package com.chat.chat.dto.response;

import java.util.List;

import com.chat.chat.entity.Room;

public record AllRoomResponse(
	List<BasicRoomResponse> rooms
) {
	public static AllRoomResponse fromAllRoomResponseDto(List<BasicRoomResponse> room){

		return new AllRoomResponse(room);
	}
}
