package com.chat.chat.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import lombok.AllArgsConstructor;

@Configuration
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
@AllArgsConstructor
public class MongoConfig extends AbstractReactiveMongoConfiguration {

	private MongoProperties mongoProperties;

	@Override
	protected String getDatabaseName() {
		return mongoProperties.getDatabase();
	}
}
