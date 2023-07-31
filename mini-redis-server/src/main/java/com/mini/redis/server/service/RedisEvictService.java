package com.mini.redis.server.service;

import com.mini.redis.server.constant.RedisMaxMemoryPolicy;
import com.mini.redis.server.struct.RedisDb;
import com.mini.redis.server.struct.RedisObject;
import com.mini.redis.server.struct.RedisServer;
import com.mini.redis.server.struct.evict.EvictionPoolEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/30 21:03
 */
@Service
@Slf4j
public class RedisEvictService {

    public final static Integer EVPOOL_SIZE = 16;

    public final static Integer RANDOM_GEY_SOME_KEYS_COUNT = 32;

    public final static Integer MEMORY_UNIT = 1024 * 1024;

    public static EvictionPoolEntry[] EvictionPoolLRU = new EvictionPoolEntry[EVPOOL_SIZE];

    public void freeMemoryIfNeeded() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();

        RedisServer redisServer = RedisServer.getRedisServer();
        int maxMemoryPolicy = redisServer.getMaxMemoryPolicy();

        long redisMaxMemory = redisServer.getMaxMemory();
        long toFreeMemory = usedMemory - redisMaxMemory;
        long freeMemory = 0;

        log.info("unit:m, totalMemory:{},usedMemory:{},maxMemory:{},toFreeMemory:{}",
                totalMemory / MEMORY_UNIT,
                usedMemory / MEMORY_UNIT,
                redisMaxMemory / MEMORY_UNIT,
                toFreeMemory / MEMORY_UNIT);
        List<RedisDb> redisDbs = redisServer.getRedisDbs();
        while (freeMemory < toFreeMemory) {
            RedisDb redisDb = null;
            String bestKey = "";
            long delta = 0;

            if (maxMemoryPolicy == RedisMaxMemoryPolicy.NOEVICTION) {
                break;
            }
            if (maxMemoryPolicy == RedisMaxMemoryPolicy.ALL_KEYS_LFU
                    || maxMemoryPolicy == RedisMaxMemoryPolicy.VOLATILE_LFU) {

            } else if (maxMemoryPolicy == RedisMaxMemoryPolicy.ALL_KEYS_LRU
                    || maxMemoryPolicy == RedisMaxMemoryPolicy.VOLATILE_LRU) {
                for (int i = 0; i < redisDbs.size(); i++) {
                    RedisDb db = redisDbs.get(i);
                    ConcurrentHashMap<String, RedisObject> dict = db.getDict();
                    ConcurrentHashMap<String, Long> expires = db.getExpires();
                    evictionPoolPopulate(i, maxMemoryPolicy, dict, expires, EvictionPoolLRU);
                }

            } else if (maxMemoryPolicy == RedisMaxMemoryPolicy.ALL_KEYS_RANDOM
                    || maxMemoryPolicy == RedisMaxMemoryPolicy.VOLATILE_RANDOM) {
                for (RedisDb db : redisDbs) {
                    //dict可以直接获取table,根据long & table.size获取一个随机数进行删除
                    //此处用的是JUC的ConcurrentHashMap代替dict，不能直接table属性（transient），随机算法暂不模拟
                    ConcurrentHashMap<String, RedisObject> dict = db.getDict();
                    ConcurrentHashMap<String, Long> expires = db.getExpires();
                    Optional<String> findKey = maxMemoryPolicy == RedisMaxMemoryPolicy.ALL_KEYS_RANDOM
                            ? dict.keySet().stream().findAny()
                            : expires.keySet().stream().findAny();
                    if (findKey.isPresent()) {
                        redisDb = db;
                        bestKey = findKey.get();
                        break;
                    }
                }
            }
            //finally del
            if (StringUtils.isNotBlank(bestKey)) {
                delta = Runtime.getRuntime().totalMemory();
                assert redisDb != null;
                ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
                ConcurrentHashMap<String, Long> expires = redisDb.getExpires();
                dict.remove(bestKey);
                expires.remove(bestKey);
                delta = delta - Runtime.getRuntime().totalMemory();
                freeMemory = freeMemory + delta;
            }
        }
    }

    /**
     * 淘汰池
     *
     * @param dbId
     * @param maxMemoryPolicy
     * @param dict
     * @param expires
     * @param EvictionPoolLRU
     */
    private void evictionPoolPopulate(int dbId,
                                      int maxMemoryPolicy,
                                      ConcurrentHashMap<String, RedisObject> dict,
                                      ConcurrentHashMap<String, Long> expires,
                                      EvictionPoolEntry[] EvictionPoolLRU) {
        for (int i = 0; i < RANDOM_GEY_SOME_KEYS_COUNT; i++) {

        }

    }
}
