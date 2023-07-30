package com.mini.redis.server.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/11 11:08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisClient implements Serializable {

    private String clientIp;

    /**
     * 指向当前db
     */
    private RedisDb currentDb;

}
