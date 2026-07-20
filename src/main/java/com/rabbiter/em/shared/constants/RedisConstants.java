package com.rabbiter.em.shared.constants;

public class RedisConstants {
    public static final String USER_TOKEN_KEY = "user:token:";
    public static final Integer USER_TOKEN_TTL = 180;

    public static final String GOOD_TOKEN_KEY = "good:id:";
    public static final Integer GOOD_TOKEN_TTL = 30;

    // 防护相关常量
    public static final String LOCK_GOOD_KEY = "lock:good:"; // 互斥锁前缀，防缓存击穿
    public static final String NULL_CACHE_FLAG = "__NULL__";  // 空值占位，防缓存穿透
    public static final int NULL_CACHE_TTL_SECONDS = 60;       // 空值短TTL
    public static final int RANDOM_TTL_JITTER_SECONDS = 300;   // 随机抖动，防雪崩
}
