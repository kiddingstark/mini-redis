package com.mini.redis.server.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description 模拟server.h中的redisDb struct
 * @date: 2023/7/11 10:48
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisDb implements Serializable {

    /**
     * database id
     */
    private Integer id;

    /**
     * 键空间
     */
    private ConcurrentHashMap<String, RedisObject> dict;

    /**
     * 键过期时间
     */
    private ConcurrentHashMap<String, Long> expires;
}
