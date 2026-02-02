package com.pragma.test.infrastructure.drivenadapters.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * Redis adapter for User.
 * Implements caching using Redis.
 * 
 * Note: This class is automatically registered as a Spring bean
 * through component scanning configured in BeanConfiguration.
 */
public class UserRepositoryAdapter {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;
  private static final String KEY_PREFIX = "user:";
  private static final long TTL_SECONDS = 3600; // 1 hour

  public UserRepositoryAdapter(
      ReactiveRedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Saves an entity to Redis cache.
   */
  public Mono<Object> save(String id, Object entity) {
    String key = KEY_PREFIX + id;
    
    return redisTemplate.opsForValue()
        .set(key, entity, Duration.ofSeconds(TTL_SECONDS))
        .thenReturn(entity);
  }

  /**
   * Finds an entity by ID from Redis cache.
   */
  public Mono<Object> findById(String id) {
    String key = KEY_PREFIX + id;
    
    return redisTemplate.opsForValue()
        .get(key);
  }

  /**
   * Finds all entities from Redis cache.
   */
  public Flux<Object> findAll() {
    String pattern = KEY_PREFIX + "*";
    
    return redisTemplate.keys(pattern)
        .flatMap(key -> redisTemplate.opsForValue().get(key));
  }

  /**
   * Deletes an entity by ID from Redis cache.
   */
  public Mono<Boolean> deleteById(String id) {
    String key = KEY_PREFIX + id;
    
    return redisTemplate.delete(key)
        .map(count -> count > 0);
  }

  /**
   * Checks if an entity exists in Redis cache.
   */
  public Mono<Boolean> existsById(String id) {
    String key = KEY_PREFIX + id;
    
    return redisTemplate.hasKey(key);
  }
}
