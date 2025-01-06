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
import com.chat.chat.entity.Member;
import com.chat.chat.handler.MemberHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class MemberRouter {
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/api/auth/register",
			method = RequestMethod.POST,

			operation = @Operation(
				tags = "Member",
				description = "사용자 회원가입 API - 사용자 아이디 , 비밀번호, 비밀번호 확인을 통하여 회원가입 ",
				operationId = "registerMember",
				summary = "사용자 회원가입 API",
				requestBody = @RequestBody(
					required = true,
					description = " 바디 필수값 - 사용자 아이디 , 비밀번호, 비밀번호 확인",
					content = @Content(
						schema = @Schema(implementation = MemberRequest.class)
					)
				),
				parameters = {@Parameter(in = ParameterIn.PATH, name = "roomId", description = "room id")},
				responses = {
					@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Member.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
		@RouterOperation(
			path = "/api/auth/login",
			method = RequestMethod.POST,
			operation = @Operation(
				tags = "Member",
				operationId = "postMessage",
				requestBody = @RequestBody(
					required = true,
					description = " 바디 필수값 - 사용자 아이디 , 비밀번호",
					content = @Content(
						schema = @Schema(implementation = MemberRequest.class)
					)
				),
				description = "사용자 로그인 API - 사용자 아이디와 비밀번호를 통하여 로그인",
				summary = "사용자 로그인 API",
				responses = {
					@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Member.class))),
					@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
				}
			)
		),
	})
	public RouterFunction<ServerResponse> memberRouters(MemberHandler memberHandler) {
		return RouterFunctions
			.route(RequestPredicates.POST("/api/auth/register"), memberHandler::createNewMemberHandler)
			.andRoute(RequestPredicates.POST("/api/auth/login"), memberHandler::loginMemberHandler);
	}
}
