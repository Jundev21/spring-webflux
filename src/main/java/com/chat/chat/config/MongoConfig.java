package com.chat.chat.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableReactiveMongoAuditing
@RequiredArgsConstructor
@ConfigurationProperties("spring.data.mongodb")
public class MongoConfig extends AbstractReactiveMongoConfiguration {

	private final MongoProperties mongoProperties;

	@Override
	protected String getDatabaseName() {
		return mongoProperties.getDatabase();
	}
	@Bean
	ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
		return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
	}
}
