package com.rabbiter.em.shared.cache;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 1. 定义起始时间戳（使用当前时间的前几天，确保不会超出31位范围）
    // 将起始时间戳设置为当前时间前大约20天，这样可以确保有足够的空间
    private static final long START_TIMESTAMP = System.currentTimeMillis() - (20L * 24 * 60 * 60 * 1000);

    // 2. 定义序列号的位数（32位，与时间戳31位合计63位，不超Long范围）
    private static final int SEQUENCE_BITS = 32;

    // 3. 序列号的最大值（用于校验，避免溢出，2^32-1）
    private static final long SEQUENCE_MAX = (1L << SEQUENCE_BITS) - 1;

    public long nextId(String keyPrefix) {
        // 第一步：生成「相对时间戳」（当前时间戳 - 起始时间戳），减少位数到31位内
        long currentTimestamp = System.currentTimeMillis();
        long relativeTimestamp = currentTimestamp - START_TIMESTAMP;
        
        // 调试信息
        System.out.println("当前时间戳: " + currentTimestamp);
        System.out.println("起始时间戳: " + START_TIMESTAMP);
        System.out.println("相对时间戳: " + relativeTimestamp);
        System.out.println("31位最大值: " + ((1L << 31) - 1));
        
        // 校验：如果相对时间戳超过31位，说明起始时间戳需要更新
        if (relativeTimestamp > (1L << 31) - 1) {
            // 计算超出的位数
            long max31BitValue = (1L << 31) - 1;
            System.err.println("时间戳超出范围！相对时间戳: " + relativeTimestamp + ", 最大允许值: " + max31BitValue);
            throw new RuntimeException("时间戳超出范围，需更新起始时间戳");
        }

        // 第二步：生成每日重置的序列号（用Redis自增，保证原子性）
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String redisKey = "icr:" + keyPrefix + ":" + dateKey;
        // 调用Redis自增：key不存在时从1开始，每天自动重置（因为dateKey变了）
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        // 校验：序列号超出32位最大值（理论上不会触发，32位支持每天42亿个ID）
        if (sequence == null || sequence > SEQUENCE_MAX) {
            throw new RuntimeException("当前序列号超出上限，请稍后再试");
        }

        // 第三步：拼接ID（时间戳左移32位，再与序列号做或运算）
        // 原理：时间戳占高31位，序列号占低32位，两者无重叠，或运算后合并为63位
        return (relativeTimestamp << SEQUENCE_BITS) | sequence;
    }


}