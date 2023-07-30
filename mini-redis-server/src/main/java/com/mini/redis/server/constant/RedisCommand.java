package com.mini.redis.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: zhengruihong
 * @description 模拟server.h中的redisCommand struct
 * @date: 2023/7/22 13:33
 */

@Getter
@AllArgsConstructor
public enum RedisCommand {

    SELECT("select", "selectCommand", 2),

    EXPIRE("expire", "expireCommand", 3),

    TTL("ttl", "ttlCommand", 2),

    DEL("del", "delCommand", -2),

    OBJECT("object", "object", -2),

    SET("set", "setCommand", 3),

    GET("get", "getCommand", 2),

    APPEND("append", "appendCommand", 3),

    INCR("incr", "incrCommand", 2),

    DECR("decr", "decrCommand", 2),

    SADD("sadd", "saddCommand", -3),

    SMEMBERS("smembers", "smembersCommand", 2),

    SPOP("spop", "spopCommand", -2),

    LPUSH("lpush", "lpushCommand", -3),

    RPUSH("rpush", "rpushCommand", -3),

    LPOP("lpop", "lpopCommand", 2),

    RPOP("rpop", "rpopCommand", 2),

    LINDEX("lindex", "lindexCommand", 3),

    LLEN("llen", "llenCommand", 2),

    LINSERT("linsert", "linsertCommand", 5),

    LRANGE("lrange", "lrangeCommand", 4),

    HMSET("hmset", "hmsetCommand", -4),

    HMGET("hmget", "hmgetCommand", -3),

    HGETALL("hgetall", "hgetallCommand", 2),

    HLEN("hlen", "hlenCommand", 2),

    HKEYS("hkeys", "hkeysCommand", 2),

    HVALS("hvals", "hvalsCommand", 2),

    ZADD("zadd", "zaddCommand", -4),

    ZRANK("zrank", "zrankCommand", 3),

    ;

    private final String commandType;

    private final String serviceMethod;

    /**
     * >0:参数条目必须等于arity
     * <0:参数条目必须大于/等于arity
     */
    private final Integer arity;

    public static RedisCommand getRedisCommandByCommandType(String commandType) {
        for (RedisCommand redisCommand : values()) {
            if (redisCommand.getCommandType().equals(commandType)) {
                return redisCommand;
            }
        }
        return null;
    }
}
