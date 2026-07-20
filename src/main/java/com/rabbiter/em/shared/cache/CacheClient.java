package com.rabbiter.em.shared.cache;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.rabbiter.em.shared.constants.RedisConstants.*;

@Component
public class CacheClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheClient(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T get(String key,
                     Class<T> clazz,
                     int ttlMinutes,
                     Function<String, T> dbLoader) {
        // 1) 直接查询Redis
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Object cached = valueOps.get(key);
        if (cached != null) {
            if (Objects.equals(cached, NULL_CACHE_FLAG)) {
                return null;
            }
            return clazz.cast(cached);
        }

        // 2) 互斥锁，防击穿
        String lockKey = LOCK_GOOD_KEY + key;
        boolean locked = tryLock(lockKey);
        try {
            if (!locked) {
                // 竞争失败，短暂休眠后重试一次
                sleepSilently(50);
                cached = valueOps.get(key);
                if (cached != null) {
                    if (Objects.equals(cached, NULL_CACHE_FLAG)) {
                        return null;
                    }
                    return clazz.cast(cached);
                }
            }

            // 3) 加载数据库
            T data = dbLoader.apply(key);
            if (data == null) {
                // 空值写入短TTL，防穿透
                valueOps.set(key, NULL_CACHE_FLAG, Duration.ofSeconds(NULL_CACHE_TTL_SECONDS));
                return null;
            }

            // 4) 写入Redis
            valueOps.set(key, data, Duration.ofSeconds(ttlMinutes * 60L));
            return data;
        } finally {
            if (locked) {
                unlock(lockKey);
            }
        }
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }

    private boolean tryLock(String key) {
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(ok);
        } catch (DataAccessException e) {
            return false;
        }
    }

    private void unlock(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}