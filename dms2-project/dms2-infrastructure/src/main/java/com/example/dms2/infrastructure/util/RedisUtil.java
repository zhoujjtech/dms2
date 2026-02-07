package com.example.dms2.infrastructure.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RDeque;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.stereotype.Component;

/**
 * Redis 工具类（基于 Redisson）
 *
 * <p>提供了 Redis 常用操作的封装方法，包括： String/Hash/Set/List/ZSet/Bitmap/HyperLogLog
 * 等数据结构的操作，以及分布式锁、分布式集合等高级功能
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedissonClient redissonClient;

  /** ============================= String 相关操作 ============================= */

  /**
   * 设置缓存
   *
   * @param key 键
   * @param value 值
   */
  public void set(String key, Object value) {
    RBucket<Object> bucket = redissonClient.getBucket(key);
    bucket.set(value);
  }

  /**
   * 设置缓存并设置过期时间
   *
   * @param key 键
   * @param value 值
   * @param timeout 过期时间
   * @param unit 时间单位
   */
  public void set(String key, Object value, long timeout, TimeUnit unit) {
    RBucket<Object> bucket = redissonClient.getBucket(key);
    bucket.set(value, timeout, unit);
  }

  /**
   * 获取缓存
   *
   * @param key 键
   * @return 值
   */
  public Object get(String key) {
    RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.get();
  }

  /**
   * 获取缓存（指定类型）
   *
   * @param key 键
   * @param clazz 类型
   * @return 值
   */
  public <T> T get(String key, Class<T> clazz) {
    RBucket<T> bucket = redissonClient.getBucket(key);
    return bucket.get();
  }

  /**
   * 删除缓存
   *
   * @param key 键
   * @return 是否成功
   */
  public Boolean delete(String key) {
    return redissonClient.getBucket(key).delete();
  }

  /**
   * 批量删除缓存
   *
   * @param keys 键集合
   * @return 删除数量
   */
  public Long delete(Collection<String> keys) {
    return redissonClient.getKeys().delete(keys.toArray(new String[0]));
  }

  /**
   * 设置过期时间
   *
   * @param key 键
   * @param timeout 过期时间
   * @param unit 时间单位
   * @return 是否成功
   */
  public Boolean expire(String key, long timeout, TimeUnit unit) {
    RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.expire(timeout, unit);
  }

  /**
   * 获取过期时间
   *
   * @param key 键
   * @return 过期时间（秒）
   */
  public long getExpire(String key) {
    RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.remainTimeToLive();
  }

  /**
   * 判断 key 是否存在
   *
   * @param key 键
   * @return 是否存在
   */
  public Boolean hasKey(String key) {
    return redissonClient.getBucket(key).isExists();
  }

  /** ============================= Hash 相关操作 ============================= */

  /**
   * 设置 Hash
   *
   * @param key 键
   * @param hashKey Hash 键
   * @param value 值
   */
  public void hSet(String key, String hashKey, Object value) {
    RMap<String, Object> map = redissonClient.getMap(key);
    map.put(hashKey, value);
  }

  /**
   * 获取 Hash
   *
   * @param key 键
   * @param hashKey Hash 键
   * @return 值
   */
  public Object hGet(String key, String hashKey) {
    RMap<String, Object> map = redissonClient.getMap(key);
    return map.get(hashKey);
  }

  /**
   * 获取所有 Hash
   *
   * @param key 键
   * @return Map
   */
  public Map<String, Object> hGetAll(String key) {
    RMap<String, Object> map = redissonClient.getMap(key);
    return map.readAllMap();
  }

  /**
   * 删除 Hash
   *
   * @param key 键
   * @param hashKeys Hash 键集合
   * @return 删除数量
   */
  public long hDelete(String key, Object... hashKeys) {
    RMap<String, Object> map = redissonClient.getMap(key);
    // Convert Object[] to String[] for fastRemove
    String[] keys = new String[hashKeys.length];
    for (int i = 0; i < hashKeys.length; i++) {
      keys[i] = hashKeys[i].toString();
    }
    return map.fastRemove(keys);
  }

  /**
   * 判断 Hash 键是否存在
   *
   * @param key 键
   * @param hashKey Hash 键
   * @return 是否存在
   */
  public Boolean hHasKey(String key, String hashKey) {
    RMap<String, Object> map = redissonClient.getMap(key);
    return map.containsKey(hashKey);
  }

  /**
   * 带 TTL 的 Hash Map（适合缓存场景）
   *
   * @param key 键
   * @return RMapCache
   */
  public <K, V> RMapCache<K, V> getMapCache(String key) {
    return redissonClient.getMapCache(key);
  }

  /** ============================= Set 相关操作 ============================= */

  /**
   * 设置 Set
   *
   * @param key 键
   * @param values 值集合
   * @return 是否成功
   */
  public boolean sAdd(String key, Object... values) {
    RSet<Object> set = redissonClient.getSet(key);
    for (Object value : values) {
      set.add(value);
    }
    return true;
  }

  /**
   * 获取 Set 所有值
   *
   * @param key 键
   * @return Set 集合
   */
  public Set<Object> sMembers(String key) {
    RSet<Object> set = redissonClient.getSet(key);
    // Use stream to collect all elements
    return java.util.stream.StreamSupport.stream(set.spliterator(), false)
        .collect(java.util.stream.Collectors.toSet());
  }

  /**
   * 判断 Set 中是否存在值
   *
   * @param key 键
   * @param value 值
   * @return 是否存在
   */
  public Boolean sIsMember(String key, Object value) {
    RSet<Object> set = redissonClient.getSet(key);
    return set.contains(value);
  }

  /**
   * 获取 Set 长度
   *
   * @param key 键
   * @return 长度
   */
  public int sSize(String key) {
    RSet<Object> set = redissonClient.getSet(key);
    return set.size();
  }

  /**
   * 删除 Set 中的值
   *
   * @param key 键
   * @param values 值集合
   * @return 删除数量
   */
  public boolean sRemove(String key, Object... values) {
    RSet<Object> set = redissonClient.getSet(key);
    return set.removeAll(List.of(values));
  }

  /**
   * 获取 Set 对象（用于更多操作）
   *
   * @param key 键
   * @return RSet
   */
  public <V> RSet<V> getSet(String key) {
    return redissonClient.getSet(key);
  }

  /**
   * 获取 SetMultimap（多值 Set）
   *
   * @param key 键
   * @return RSetMultimap
   */
  public <K, V> RSetMultimap<K, V> getSetMultimap(String key) {
    return redissonClient.getSetMultimap(key);
  }

  /** ============================= List 相关操作 ============================= */

  /**
   * 设置 List（左侧添加）
   *
   * @param key 键
   * @param value 值
   */
  public void lLeftPush(String key, Object value) {
    RList<Object> list = redissonClient.getList(key);
    // Redisson RList doesn't have leftPush, use add at index 0
    list.add(0, value);
  }

  /**
   * 设置 List（右侧添加）
   *
   * @param key 键
   * @param value 值
   */
  public void lRightPush(String key, Object value) {
    RList<Object> list = redissonClient.getList(key);
    // Redisson RList doesn't have rightPush, use add
    list.add(value);
  }

  /**
   * 获取 List 范围内的值
   *
   * @param key 键
   * @param start 开始位置
   * @param end 结束位置
   * @return List
   */
  public List<Object> lRange(String key, int start, int end) {
    RList<Object> list = redissonClient.getList(key);
    return list.range(start, end);
  }

  /**
   * 获取 List 所有值
   *
   * @param key 键
   * @return List
   */
  public List<Object> lRange(String key) {
    RList<Object> list = redissonClient.getList(key);
    return list.readAll();
  }

  /**
   * 获取 List 长度
   *
   * @param key 键
   * @return 长度
   */
  public int lSize(String key) {
    RList<Object> list = redissonClient.getList(key);
    return list.size();
  }

  /**
   * 根据索引获取 List 中的值
   *
   * @param key 键
   * @param index 索引
   * @return 值
   */
  public Object lIndex(String key, int index) {
    RList<Object> list = redissonClient.getList(key);
    return list.get(index);
  }

  /**
   * 删除 List 中的值
   *
   * @param key 键
   * @param count 删除数量
   * @param value 值
   * @return 删除数量
   */
  public int lRemove(String key, int count, Object value) {
    RList<Object> list = redissonClient.getList(key);
    // Redisson RList doesn't have a remove method with count parameter
    // We'll remove all occurrences
    int removed = 0;
    while (list.contains(value)) {
      list.remove(value);
      removed++;
    }
    return removed;
  }

  /**
   * 获取 Deque（双端队列）
   *
   * @param key 键
   * @return RDeque
   */
  public <V> RDeque<V> getDeque(String key) {
    return redissonClient.getDeque(key);
  }

  /** ============================= ZSet 相关操作 ============================= */

  /**
   * 设置 ZSet
   *
   * @param key 键
   * @param value 值
   * @param score 分数
   * @return 是否成功
   */
  public boolean zAdd(String key, Object value, double score) {
    RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
    return sortedSet.add(score, value);
  }

  /**
   * 获取 ZSet 范围内的值（按分数）
   *
   * @param key 键
   * @param start 开始分数
   * @param end 结束分数
   * @return 集合
   */
  public Collection<Object> zRangeByScore(String key, double start, double end) {
    RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
    return sortedSet.valueRange(start, true, end, true);
  }

  /**
   * 获取 ZSet 起围内的值（按排名）
   *
   * @param key 键
   * @param start 开始排名
   * @param end 结束排名
   * @return 集合
   */
  public Collection<Object> zRange(String key, int start, int end) {
    RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
    return sortedSet.valueRange(start, end);
  }

  /**
   * 删除 ZSet 中的值
   *
   * @param key 键
   * @param values 值集合
   * @return 删除数量
   */
  public long zRemove(String key, Object... values) {
    RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
    long count = 0;
    for (Object value : values) {
      if (sortedSet.remove(value)) {
        count++;
      }
    }
    return count;
  }

  /**
   * 增加 ZSet 分数
   *
   * @param key 键
   * @param value 值
   * @param delta 增量
   * @return 新分数
   */
  public double zIncrementScore(String key, Object value, double delta) {
    RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
    return sortedSet.addScore(value, delta);
  }

  /** ============================= 分布式锁 ============================= */

  /**
   * 获取分布式锁
   *
   * @param lockName 锁名称
   * @return 锁对象
   */
  public Object getLock(String lockName) {
    return redissonClient.getLock(lockName);
  }

  /**
   * 获取公平锁
   *
   * @param lockName 锁名称
   * @return 公平锁对象
   */
  public Object getFairLock(String lockName) {
    return redissonClient.getFairLock(lockName);
  }

  /**
   * 获取读写锁
   *
   * @param lockName 锁名称
   * @return 读写锁对象
   */
  public Object getReadWriteLock(String lockName) {
    return redissonClient.getReadWriteLock(lockName);
  }

  /** ============================= 其他高级功能 ============================= */

  /**
   * 获取 HyperLogLog 对象（用于基数统计）
   *
   * @param key 键
   * @return HyperLogLog 对象
   */
  public <V> RHyperLogLog<V> getHyperLogLog(String key) {
    return redissonClient.getHyperLogLog(key);
  }

  /**
   * 获取计数器
   *
   * @param key 键
   * @return 计数器对象
   */
  public org.redisson.api.RAtomicLong getAtomicLong(String key) {
    return redissonClient.getAtomicLong(key);
  }

  /**
   * 获取倒计数器
   *
   * @param key 键
   * @return 倒计数器对象
   */
  public RCountDownLatch getCountDownLatch(String key) {
    return redissonClient.getCountDownLatch(key);
  }

  /**
   * 获取 Topic（发布订阅）
   *
   * @param key 键
   * @return Topic 对象
   */
  @SuppressWarnings("unchecked")
  public RTopic getTopic(String key) {
    return redissonClient.getTopic(key);
  }

  /**
   * 批量操作
   *
   * @return 批量操作对象
   */
  public RBatch createBatch() {
    return redissonClient.createBatch();
  }

  /**
   * 获取原生 RedisClient（用于高级操作）
   *
   * @param redisNodes Redis 节点地址
   * @return RedisClient
   */
  public RedisClient getRedisClient(String... redisNodes) {
    // Redisson.create() requires Config, not String array
    // Returning null as this method is not recommended for use
    throw new UnsupportedOperationException(
        "Direct RedisClient creation is not supported. Use redissonClient instead.");
  }

  /**
   * 获取原生 RedissonClient（用于高级操作）
   *
   * @return RedissonClient
   */
  public RedissonClient getRedissonClient() {
    return redissonClient;
  }
}
