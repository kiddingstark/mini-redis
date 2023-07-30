package com.mini.redis.server.schedule;

import com.mini.redis.server.struct.RedisDb;
import com.mini.redis.server.struct.RedisObject;
import com.mini.redis.server.struct.RedisServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/13 10:12
 */
@Component
@Slf4j
public class RedisServerSchedule {

    public final static Integer EXPIRE_DEL_COUNT = 500;

    @Scheduled(cron = "0/5 * * * * ? ")
    public void delExpireKeySchedule() {
        RedisServer redisServer = RedisServer.getRedisServer();
        for (RedisDb redisDb : redisServer.getRedisDbs()) {
            ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
            ConcurrentHashMap<String, Long> expires = redisDb.getExpires();
            int delNum = 0;
            while (delNum < EXPIRE_DEL_COUNT) {
                for (String key : expires.keySet()) {
                    if (expires.get(key) <= System.currentTimeMillis()) {
                        expires.remove(key);
                        dict.remove(key);
                        delNum++;
                        log.info("schedule del expire key:{}", key);
                    }
                }
            }
        }
    }
}
