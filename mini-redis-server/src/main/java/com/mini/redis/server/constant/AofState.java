package com.mini.redis.server.constant;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/11 11:13
 */
public interface AofState {

    Integer OFF = 0;

    Integer ON = 1;

    Integer WAIT_REWRITE = 2;
}
