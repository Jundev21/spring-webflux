package com.chat.chat.dto.response;

import java.time.LocalDateTime;

import com.chat.chat.entity.Member;
import com.chat.chat.entity.Room;

public record BasicMemberResponse(
	String memberId,
	LocalDateTime localDateTime
) {
	public static BasicMemberResponse basicMemberResponse(Member member){
		return new BasicMemberResponse(member.getMemberId(), member.getCreatedDate());
	}

}
