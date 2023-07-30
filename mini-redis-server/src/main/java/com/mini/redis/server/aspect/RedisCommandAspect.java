package com.mini.redis.server.aspect;

import com.mini.redis.server.struct.RedisClient;
import com.mini.redis.server.struct.RedisDb;
import com.mini.redis.server.struct.RedisObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/29 23:51
 */
@Component
@Aspect
@Slf4j
public class RedisCommandAspect {

    @Pointcut("execution(* com.mini.redis.server.service.RedisCommandService.*(..))")
    public void normalRedisComamnd() {
    }

    @Pointcut("execution(* com.mini.redis.server.service.RedisCommandService.selectCommand())")
    public void skipRedisComamnd() {
    }

    @Pointcut("normalRedisComamnd() && !skipRedisComamnd()")
    public void redisCommand() {
    }

    /**
     * schedule del +  lazy del
     *
     * @param joinPoint
     * @see com.mini.redis.server.schedule.RedisServerSchedule
     */
    @Before("redisCommand()")
    public void lazyDelBeforeExec(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        RedisClient redisClient = (RedisClient) args[0];
        String[] argv = (String[]) args[1];

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        ConcurrentHashMap<String, Long> expires = redisDb.getExpires();
        String key = argv[1];
        if (!expires.containsKey(key)) {
            return;
        }
        long expireTimeStamp = expires.get(key);
        if (expireTimeStamp > System.currentTimeMillis()) {
            return;
        }
        log.info("lazy del expire key:{}", key);
        dict.remove(key);
        expires.remove(key);
    }

    /**
     * udpate lru and refCount
     *
     * @param joinPoint
     */
    @AfterReturning("redisCommand()")
    public void updateRedisObjectAfterExec(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        RedisClient redisClient = (RedisClient) args[0];
        String[] argv = (String[]) args[1];

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        String key = argv[1];
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return;
        }
        redisObject.setRefCount(redisObject.getRefCount() + 1);
        redisObject.setLru(System.currentTimeMillis());
        log.info("update redis object lru:{}, refCount:{}", redisObject.getLru(), redisObject.getRefCount());
    }
}
