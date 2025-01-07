package com.chat.chat.router;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.chat.chat.dto.request.MemberRequest;
import com.chat.chat.dto.response.ErrorResponse;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.handler.UserInfoHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class UserInfoRouter {

	// 회원 정보 불러오기 -> 어차피 토큰으로 받음
	// 회원 정보 수정하기
	// 회원 삭제 하기
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/api/user",
			method = RequestMethod.GET,
			operation = @Operation(
				tags = "Member",
				description = "사용자 검색 API - JWT 를 통하여 사용자 아이디 사용",
				operationId = "searchMember",
				summary = "사용자 검색 API",
				responses = {
					@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MemberResponse.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/user",
			method = RequestMethod.PATCH,
			operation = @Operation(
				tags = "Member",
				operationId = "updateUserInfo",
				requestBody = @RequestBody(
					required = true,
					description = " 바디 필수값 - 사용자 아이디 , 비밀번호",
					content = @Content(
						schema = @Schema(implementation = MemberRequest.class)
					)
				),
				description = "회원 정보 수정 API - 사용자 아이디와 비밀번호를 통하여 로그인",
				summary = "회원 정보 수정 API",
				responses = {
					@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Member.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/user",
			method = RequestMethod.DELETE,
			operation = @Operation(
				tags = "Member",
				operationId = "postMessage",
				summary = "회원탈퇴 API",
				responses = {
					@ApiResponse(responseCode = "201", content = @Content(mediaType = "사용자 삭제 성공")),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
	})
	public RouterFunction<ServerResponse> UserInfoRouters(UserInfoHandler userInfoHandler) {
		return RouterFunctions
			.route(RequestPredicates.GET("/api/user"), userInfoHandler::retrievedUserInfoHandler)
			.andRoute(RequestPredicates.PATCH("api/user"), userInfoHandler::editUserInfoHandler)
			.andRoute(RequestPredicates.DELETE("/api/user"), userInfoHandler::deleteUserInfoHandler);
	}
}
