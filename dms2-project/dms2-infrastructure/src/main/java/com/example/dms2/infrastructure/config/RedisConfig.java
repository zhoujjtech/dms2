package com.example.dms2.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类（使用 Redisson）
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Value("${spring.data.redis.database:0}")
  private int redisDatabase;

  @Value("${spring.data.redis.timeout:5000}")
  private int redisTimeout;

  /** Redisson 客户端配置 */
  @Bean(destroyMethod = "shutdown")
  public RedissonClient redissonClient() {
    Config config = new Config();

    // 单服务器配置
    config
        .useSingleServer()
        .setAddress("redis://" + redisHost + ":" + redisPort)
        .setDatabase(redisDatabase)
        .setPassword(redisPassword)
        .setConnectionPoolSize(64)
        .setConnectionMinimumIdleSize(10)
        .setTimeout(redisTimeout)
        .setRetryAttempts(3)
        .setRetryInterval(1500)
        .setKeepAlive(true);

    // 如果使用集群，可以切换为集群配置
    // config.useClusterServers()
    //     .addNodeAddress("redis://node1:6379", "redis://node2:6379", "redis://node3:6379");

    return Redisson.create(config);
  }

  /** RedisTemplate 配置 */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);

    // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(Object.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

    // String 序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // key 采用 String 的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // hash 的 key 也采用 String 的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);
    // value 序列化方式采用 jackson
    template.setValueSerializer(jackson2JsonRedisSerializer);
    // hash 的 value 序列化方式采用 jackson
    template.setHashValueSerializer(jackson2JsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }

  /** CacheManager 配置 */
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    // 配置序列化
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(Object.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

    // 配置序列化
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // 默认缓存时间1小时
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    jackson2JsonRedisSerializer))
            .disableCachingNullValues(); // 不缓存空值

    return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
  }

  /** Redisson 连接工厂 */
  @Bean
  public RedisConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
    return new RedissonConnectionFactory(redissonClient);
  }
}
