package com.mini.redis.server.service;

import com.google.common.collect.Sets;
import com.mini.redis.server.constant.RedisObjectEncoding;
import com.mini.redis.server.constant.RedisObjectType;
import com.mini.redis.server.struct.RedisObject;
import com.mini.redis.server.struct.base.IntSet;
import com.mini.redis.server.struct.base.RedisLinkedList;
import com.mini.redis.server.struct.base.ZSet;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/13 22:15
 */
@Service
public class RedisObjectService {

    /**
     * int or string sds init
     *
     * @param value
     * @return
     */
    public RedisObject getStringObject(Object value) {
        if (Objects.isNull(value)) {
            return new RedisObject();
        }
        RedisObject redisObject = RedisObject.builder()
                .type(RedisObjectType.REDIS_STRING)
                .build();
        String str = value.toString();
        try {
            Integer num = Integer.parseInt(str);
            redisObject.setEncoding(RedisObjectEncoding.OBJ_ENCODING_INT.getCode());
            redisObject.setValue(num);
        } catch (NumberFormatException e) {
            //redis embstr编码调用一次内存分配函数分配 redisObject + sds，而raw调用两次
            redisObject.setEncoding(str.getBytes(StandardCharsets.UTF_8).length > 39
                    ? RedisObjectEncoding.OBJ_ENCODING_RAW.getCode()
                    : RedisObjectEncoding.OBJ_ENCODING_EMBSTR.getCode());
            redisObject.setValue(str);
        }
        return redisObject;
    }

    /**
     * intset object init
     *
     * @param values
     * @return
     */
    public RedisObject getSetObject(String[] values) {
        if (values.length == 0) {
            return new RedisObject();
        }
        Integer encoding = RedisObjectEncoding.OBJ_ENCODING_INTSET.getCode();
        RedisObject redisObject = RedisObject.builder()
                .type(RedisObjectType.REDIS_SET)
                .build();

        //get encode
        for (String object : values) {
            try {
                Integer.parseInt(object);
            } catch (NumberFormatException e) {
                encoding = RedisObjectEncoding.OBJ_ENCODING_HT.getCode();
                break;
            }
        }

        //integer使用intset
        //string使用hash,key为string值，value为null,实际也是HashSet的底层实现，所以这里统一用Java的HashSet模拟
        IntSet intSet = new IntSet();
        HashSet<String> hashSet = Sets.newHashSet();
        for (Object object : values) {
            if (encoding.equals(RedisObjectEncoding.OBJ_ENCODING_INTSET.getCode())) {
                intSet.intSetAdd(Integer.parseInt(object.toString()));
            } else {
                hashSet.add(object.toString());
            }
        }
        redisObject.setValue(encoding.equals(RedisObjectEncoding.OBJ_ENCODING_INTSET.getCode()) ? intSet : hashSet);
        redisObject.setEncoding(encoding);
        return redisObject;
    }

    /**
     * lpush object init
     *
     * @param values
     * @return
     */
    public RedisObject getLpushListObject(String[] values) {
        RedisLinkedList<String> list = new RedisLinkedList<>();
        for (String str : values) {
            list.push(str);
        }
        return RedisObject.builder()
                .type(RedisObjectType.REDIS_LIST)
                .encoding(RedisObjectEncoding.OBJ_ENCODING_LINKEDLIST.getCode())
                .value(list)
                .build();
    }

    /**
     * rpush object init
     *
     * @param values
     * @return
     */
    public RedisObject getRpushListObject(String[] values) {
        RedisLinkedList<String> list = new RedisLinkedList<>(Arrays.asList(values));
        return RedisObject.builder()
                .type(RedisObjectType.REDIS_LIST)
                .encoding(RedisObjectEncoding.OBJ_ENCODING_LINKEDLIST.getCode())
                .value(list)
                .build();
    }

    /**
     * hash object init
     *
     * @param map
     * @return
     */
    public RedisObject getHashObject(String[] map) {
        //todo hash可以选择用ziplist/hash实现底层数据结构
        if (map.length == 0) {
            return new RedisObject();
        }
        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < map.length - 1; i = i + 2) {
            hashMap.put(map[i], map[i + 1]);
        }
        return RedisObject.builder()
                .type(RedisObjectType.REDIS_HASH)
                .encoding(RedisObjectEncoding.OBJ_ENCODING_HT.getCode())
                .value(hashMap)
                .build();
    }

    /**
     * zset object init
     *
     * @param values
     * @return
     */
    public RedisObject getZSetObject(String[] values) {
        if (values.length == 0) {
            return new RedisObject();
        }
        ZSet zSet = new ZSet();
        for (int i = 0; i < values.length - 1; i = i + 2) {
            zSet.zadd(Double.parseDouble(values[i]), values[i + 1]);
        }

        return RedisObject.builder()
                .type(RedisObjectType.REDIS_ZSET)
                .encoding(RedisObjectEncoding.OBJ_ENCODING_ZIPLIST.getCode())
                .value(zSet)
                .build();
    }
}
