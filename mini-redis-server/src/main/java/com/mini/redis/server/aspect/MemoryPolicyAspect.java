package com.mini.redis.server.aspect;

import com.mini.redis.server.service.RedisEvictService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/30 15:52
 */

@Component
@Aspect
@Slf4j
public class MemoryPolicyAspect {

    @Pointcut("execution(* com.mini.redis.server.service.RedisCommandService.* (..))")
    public void processCommand() {
    }


    @Autowired
    private RedisEvictService redisEvictService;

    /**
     * 内存淘汰
     */
    @Before("processCommand()")
    public void maxMemoryPolicy() {
        redisEvictService.freeMemoryIfNeeded();
    }
}
