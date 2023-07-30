package com.mini.redis.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/12 00:05
 */
@Getter
@AllArgsConstructor
public enum RedisObjectEncoding {

    OBJ_ENCODING_RAW(0, "raw"), /* Raw representation */

    OBJ_ENCODING_INT(1, "int"),    /* Encoded as  */

    OBJ_ENCODING_HT(2, "hash"),    /* Encoded as hash table */

    OBJ_ENCODING_ZIPMAP(3, "zipmap"),  /* Encoded as zipmap */

    OBJ_ENCODING_LINKEDLIST(4, "linkedlist"), /* No longer used: old list encoding. */

    OBJ_ENCODING_ZIPLIST(5, "ziplist"), /* Encoded as ziplist */

    OBJ_ENCODING_INTSET(6, "inset"),  /* Encoded as intset */

    OBJ_ENCODING_SKIPLIST(7, "skiplist"), /* Encoded as skiplist */

    OBJ_ENCODING_EMBSTR(8, "embstr"), /* Embedded sds string encoding */

    OBJ_ENCODING_QUICKLIST(9, "quicklist"), /* Encoded as linked list of ziplists */

    OBJ_ENCODING_STREAM(10, "stream"), /* Encoded as a radix tree of listpacks */;

    private final Integer code;

    private final String desc;

    public static RedisObjectEncoding getByCode(Integer code) {
        for (RedisObjectEncoding constant : RedisObjectEncoding.values()) {
            if (code.equals(constant.getCode())) {
                return constant;
            }
        }
        return null;
    }
}
