package com.mini.redis.server.constant;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/30 15:28
 */
public interface RedisMaxMemoryPolicy {

    Integer VOLATILE_RANDOM = 0;

    Integer ALL_KEYS_RANDOM = 1;

    Integer VOLATILE_LRU = 2;

    Integer ALL_KEYS_LRU = 3;

    Integer VOLATILE_LFU = 4;

    Integer ALL_KEYS_LFU = 5;

    Integer NOEVICTION = 6;
}
