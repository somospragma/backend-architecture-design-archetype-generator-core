package com.example.localtest.infrastructure.drivenadapters.mongodb;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// MODIFIED AGAIN - Second modification for hot reload test
/**
 * MongoDB adapter for Product.
 * Implements persistence operations using MongoDB.
 * 
 * Note: This class is automatically registered as a Spring bean
 * through component scanning configured in BeanConfiguration.
 */
@Component
public class ProductRepositoryAdapter {

  private final ProductRepositoryMongoRepository repository;

  public ProductRepositoryAdapter(ProductRepositoryMongoRepository repository) {
    this.repository = repository;
  }

  /**
   * Saves an entity to MongoDB.
   */
  public Mono<Object> save(Object entity) {
    return repository.save(entity);
  }

  /**
   * Finds an entity by ID from MongoDB.
   */
  public Mono<Object> findById(String id) {
    return repository.findById(id);
  }

  /**
   * Finds all entities from MongoDB.
   */
  public Flux<Object> findAll() {
    return repository.findAll();
  }

  /**
   * Deletes an entity by ID from MongoDB.
   */
  public Mono<Void> deleteById(String id) {
    return repository.deleteById(id);
  }

  /**
   * Checks if an entity exists in MongoDB.
   */
  public Mono<Boolean> existsById(String id) {
    return repository.existsById(id);
  }

  /**
   * Counts all entities in MongoDB.
   */
  public Mono<Long> count() {
    return repository.count();
  }

  /**
   * Deletes all entities from MongoDB.
   */
  public Mono<Void> deleteAll() {
    return repository.deleteAll();
  }
}
