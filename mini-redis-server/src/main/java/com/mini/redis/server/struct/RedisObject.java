package com.mini.redis.server.struct;

import com.mini.redis.server.constant.RedisObjectEncoding;
import com.mini.redis.server.constant.RedisObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: zhengruihong
 * @description 模拟server.h中的redisObject struct
 * @date: 2023/7/11 10:49
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisObject implements Serializable {

    /**
     * @see RedisObjectType
     */
    private int type;

    /**
     * @see RedisObjectEncoding
     */
    private int encoding;

    /**
     * 底层数据
     */
    private Object value;

    /**
     * 引用次数 lfu
     */
    private int refCount;

    /**
     * 最近使用时间
     */
    private long lru;

}
