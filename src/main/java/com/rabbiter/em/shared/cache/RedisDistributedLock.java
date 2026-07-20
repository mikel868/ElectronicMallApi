package com.rabbiter.em.shared.cache;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁封装类
 * 基于Redisson实现，内部使用Lua脚本保证原子性
 * 同时提供自定义Lua脚本的扩展能力，展示底层实现原理
 */
@Component
public class RedisDistributedLock {

    @Resource
    private RedissonClient redissonClient;


    private static final String LOCK_PREFIX = "distributed_lock:";

    /**
     * 尝试获取锁（带默认超时）
     * @param lockKey 锁的key
     * @return 锁对象
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(LOCK_PREFIX + lockKey);
    }


    /**
     * 执行带锁的操作（自动续期）
     * @param lockKey 锁key
     * @param waitTime 等待时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws RuntimeException 如果获取锁失败
     */
    public <T> T executeWithLock(String lockKey, long waitTime, LockAction<T> action) {
        RLock lock = getLock(lockKey);
        try {
            // 尝试获取锁，等待waitTime秒，锁自动续期（leaseTime = -1）
            boolean acquired = lock.tryLock(waitTime, -1, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("获取分布式锁失败，锁键：" + lockKey);
            }

            return action.execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁过程被中断，锁键：" + lockKey, e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("执行加锁操作时发生错误，锁键：" + lockKey, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }



    /**
     * 锁操作的函数式接口
     */
    @FunctionalInterface
    public interface LockAction<T> {
        T execute() throws Exception;
    }
    
    
}