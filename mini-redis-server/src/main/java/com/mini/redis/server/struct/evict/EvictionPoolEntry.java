package com.mini.redis.server.struct.evict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/30 20:51
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvictionPoolEntry implements Serializable {

    /**
     * Object idle time (inverse frequency for LFU)
     */
    private long idle;

    /**
     * key name
     */
    private String key;

    /**
     * Cached SDS object for key name
     */
    private String cache;

    /**
     * Key DB number
     */
    private int dbId;
}
