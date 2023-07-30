package com.mini.redis.server.struct;

import com.google.common.collect.Lists;
import com.mini.redis.server.constant.AofState;
import com.mini.redis.server.constant.RedisMaxMemoryPolicy;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description 模拟server.h中的redisServer struct
 * @date: 2023/7/11 10:47
 */

@Data
public class RedisServer implements Serializable {

    private RedisServer() {
    }

    private static RedisServer redisServer = null;

    private String ip = "127.0.0.1";

    /**
     * 监听的TCP端口
     */
    private int port = 8083;

    /**
     * 配置的db总数，默认是16个
     */
    private int dbNum = 16;

    private List<RedisDb> redisDbs;

    /**
     * max client num
     */
    private int maxClientNum = 32;

    /**
     * active clients
     */
    private List<RedisClient> clients;

    private List<RedisClient> closedClients;

    /**
     * Max number of memory bytes to use
     *
     * -Xmx: 100 * 1024 * 1024
     */
    private long maxMemory = 90 * 1024 * 1024;

    /**
     * @see RedisMaxMemoryPolicy
     */
    private int maxMemoryPolicy;

    private Boolean aofEnable;

    /**
     *
     * @see AofState
     */
    private int aofState;

    private String aofFileName;

    private String aofFilePath;

    public static RedisServer getRedisServer() {
        if (Objects.nonNull(redisServer)) {
            return redisServer;
        }
        redisServer = new RedisServer();
        redisServer.setClients(new ArrayList<>());

        List<RedisDb> redisDbs = Lists.newArrayList();
        for (int i = 0; i < redisServer.getDbNum(); i++) {
            RedisDb redisDb = RedisDb.builder()
                    .id(i)
                    .dict(new ConcurrentHashMap<>())
                    .expires(new ConcurrentHashMap<>())
                    .build();
            redisDbs.add(redisDb);
        }
        redisServer.setRedisDbs(redisDbs);
        redisServer.setAofEnable(true);
        redisServer.setAofState(AofState.ON);
        return redisServer;
    }

}
