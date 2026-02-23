package com.example.localtest.infrastructure.drivenadapters.mongodb.infrastructure.driven-adapters.mongodb.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * MongoDB configuration for ProductRepository.
 * Configures reactive MongoDB client and template.
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.example.localtest.infrastructure.drivenadapters.mongodb.infrastructure.driven-adapters.mongodb")
public class ProductRepositoryMongoConfig extends AbstractReactiveMongoConfiguration {

  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Value("${spring.data.mongodb.database}")
  private String databaseName;

  @Override
  protected String getDatabaseName() {
    return databaseName;
  }

  @Override
  public MongoClient reactiveMongoClient() {
    return MongoClients.create(mongoUri);
  }

  @Bean
  public ReactiveMongoTemplate reactiveMongoTemplate() {
    return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
  }
}
