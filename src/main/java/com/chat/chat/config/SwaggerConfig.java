package com.chat.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


//https://springdoc.org/ 스웨거 레퍼런스
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(new Components())
			.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
			.title("Chat API")
			.description("Chat API")
			.contact(new Contact()
				.name("GitHub")
				.url("https://github.com/acrofutureDev/chat"))
			.version("1.0.0");
	}
}