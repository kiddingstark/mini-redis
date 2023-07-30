package com.mini.redis.server.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Sets;
import com.mini.redis.server.constant.RedisCommand;
import com.mini.redis.server.constant.RedisObjectEncoding;
import com.mini.redis.server.constant.ResultCode;
import com.mini.redis.server.struct.RedisClient;
import com.mini.redis.server.struct.RedisDb;
import com.mini.redis.server.struct.RedisObject;
import com.mini.redis.server.struct.RedisServer;
import com.mini.redis.server.struct.base.IntSet;
import com.mini.redis.server.struct.base.RedisLinkedList;
import com.mini.redis.server.struct.base.ZSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/12 00:19
 */
@Service
public class RedisCommandService {

    @Autowired
    private RedisObjectService redisObjectService;

    /**
     * select dbId
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String selectCommand(RedisClient redisClient, String... argv) {
        RedisServer redisServer = RedisServer.getRedisServer();
        int dbId;
        try {
            dbId = Integer.parseInt(argv[1]);
        } catch (NumberFormatException e) {
            return ResultCode.OUT_OF_RANGE.getMsg();
        }
        if (dbId < 0 || dbId > redisServer.getDbNum() - 1) {
            return ResultCode.SELECT_DB_EXCEEDED.getMsg();
        }
        redisClient.setCurrentDb(redisServer.getRedisDbs().get(dbId));
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * expire key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String expireCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        int second = 0;
        try {
            second = Integer.parseInt(argv[2]);
        } catch (NumberFormatException e) {
            return ResultCode.OUT_OF_RANGE.getMsg();
        }

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, Long> expires = redisDb.getExpires();
        expires.put(key, System.currentTimeMillis() + (second * 1000));
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * ttl key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String ttlCommand(RedisClient redisClient, String... argv) {
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, Long> expires = redisDb.getExpires();
        String key = argv[1];
        if (!expires.containsKey(key)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        long expireTimestamp = expires.get(key);
        long expireSecond = DateUtil.between(new Date(), new Date(expireTimestamp), DateUnit.SECOND);
        return Long.toString(expireSecond);
    }

    /**
     * del key1 key2 ...
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String delCommand(RedisClient redisClient, String... argv) {
        int delCount = 0;
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        for (int i = 1; i < argv.length; i++) {
            if (dict.containsKey(argv[i])) {
                dict.remove(argv[i]);
                delCount++;
            }
        }
        return String.valueOf(delCount);
    }

    /**
     * set key value
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String setCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        String value = argv[2];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        dict.put(key, redisObjectService.getStringObject(value));
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * get key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String getCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        return redisObject.getValue().toString();
    }

    /**
     * append key value
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String appendCommand(RedisClient redisClient, String... argv) {
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        String key = argv[1];
        String appendValue = argv[2];
        RedisObject redisObject = dict.get(key);
        if (Objects.nonNull(redisObject)) {
            redisObject.setEncoding(RedisObjectEncoding.OBJ_ENCODING_RAW.getCode());
            redisObject.setValue(redisObject.getValue().toString() + appendValue);
        }
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * incr key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String incrCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            redisObject = redisObjectService.getStringObject(1);
            dict.put(key, redisObject);
            return redisObject.getValue().toString();
        }
        if (redisObject.getEncoding() == RedisObjectEncoding.OBJ_ENCODING_INT.getCode()) {
            return ResultCode.OUT_OF_RANGE.getMsg();
        }
        int num = Integer.parseInt(redisObject.getValue().toString());
        redisObject.setValue(num + 1);
        return redisObject.getValue().toString();
    }

    /**
     * decr key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String decrCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            redisObject = redisObjectService.getStringObject(-1);
            dict.put(key, redisObject);
            return redisObject.getValue().toString();
        }
        if (redisObject.getEncoding() == RedisObjectEncoding.OBJ_ENCODING_INT.getCode()) {
            return ResultCode.OUT_OF_RANGE.getMsg();
        }
        int num = Integer.parseInt(redisObject.getValue().toString());
        redisObject.setValue(num - 1);
        return redisObject.getValue().toString();
    }

    /**
     * sadd key value1 value2 ...
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String saddCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        String[] values = new String[argv.length - 2];
        System.arraycopy(argv, 2, values, 0, values.length);

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        dict.put(key, redisObjectService.getSetObject(values));
        return String.valueOf(values.length);
    }

    /**
     * smembers key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String smembersCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        Object value = redisObject.getValue();
        Integer encoding = redisObject.getEncoding();
        if (encoding.equals(RedisObjectEncoding.OBJ_ENCODING_INTSET.getCode())) {
            Set<String> sets = Sets.newHashSet();
            IntSet intSet = (IntSet) value;
            Arrays.stream(intSet.getContents()).forEach(v -> {
                sets.add(Integer.toString(v));
            });
            return String.join("\n", sets);
        }
        HashSet<String> hashSet = (HashSet<String>) value;
        return String.join("\n", hashSet);

    }

    /**
     * lpush key value
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String lpushCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        String[] values = new String[argv.length - 2];
        System.arraycopy(argv, 2, values, 0, values.length);

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject;
        String result = String.valueOf(argv.length - 2);
        if (dict.containsKey(key)) {
            redisObject = dict.get(key);
            LinkedList<String> list = (LinkedList<String>) redisObject.getValue();
            for (String str : values) {
                list.push(str);
            }
            return result;
        }

        dict.put(key, redisObjectService.getLpushListObject(values));
        return result;
    }

    /**
     * rpush key value
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String rpushCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        String[] values = new String[argv.length - 1];
        System.arraycopy(argv, 1, values, 0, values.length);

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject;
        if (dict.containsKey(key)) {
            redisObject = dict.get(key);
            LinkedList<String> list = (LinkedList<String>) redisObject.getValue();
            list.addAll(Arrays.asList(values));
            return ResultCode.SUCCEED.getMsg();
        }

        dict.put(key, redisObjectService.getRpushListObject(values));
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * lpop key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String lpopCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        RedisLinkedList<String> list = (RedisLinkedList<String>) redisObject.getValue();
        return list.pop();
    }

    /**
     * rpop key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String rpopCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        RedisLinkedList<String> list = (RedisLinkedList<String>) redisObject.getValue();
        return list.rPop();
    }

    /**
     * lindex key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String lindexCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        RedisLinkedList<String> list = (RedisLinkedList<String>) redisObject.getValue();
        int index = 0;
        try {
            index = Integer.parseInt(argv[2]);
        } catch (NumberFormatException e) {
            return ResultCode.OUT_OF_RANGE.getMsg();
        }
        if (index > list.size() - 1) {
            return ResultCode.NUll.getMsg();
        }
        return list.get(index);
    }

    /**
     * llen key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String llenCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NO_OPS_SUCCEED.getMsg();
        }
        RedisLinkedList<String> list = (RedisLinkedList<String>) redisObject.getValue();
        return String.valueOf(list.size());
    }

    /**
     * hmset key field1 value1 ...
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hmsetCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        String[] map = new String[argv.length - 2];
        System.arraycopy(argv, 2, map, 0, map.length);
        if (map.length % 2 != 0) {
            return String.format(ResultCode.WRONG_NUMBER_ARFUMENTS.getMsg(), RedisCommand.HMSET.getCommandType());
        }

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        dict.put(key, redisObjectService.getHashObject(map));
        return ResultCode.OK.getMsg();
    }

    /**
     * hmget key field1 ...
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hmgetCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        String[] value = new String[argv.length - 2];
        System.arraycopy(argv, 2, value, 0, value.length);

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        HashMap<String, String> hashMap = Objects.nonNull(redisObject)
                ? (HashMap<String, String>) redisObject.getValue()
                : new HashMap<>();
        List<String> result = new ArrayList<>();
        for (String field : value) {
            result.add(hashMap.containsKey(field) ? hashMap.get(field) : ResultCode.NUll.getMsg());
        }
        return CollectionUtils.isNotEmpty(result) ? String.join("\n", result) : ResultCode.NUll.getMsg();
    }

    /**
     * hgetall key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hgetallCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        HashMap<String, String> hashMap = (HashMap<String, String>) redisObject.getValue();
        List<String> result = new ArrayList<>();
        for (String field : hashMap.keySet()) {
            result.add(field);
            result.add(hashMap.get(field));
        }
        return CollectionUtils.isNotEmpty(result) ? String.join("\n", result) : ResultCode.NUll.getMsg();
    }

    /**
     * hlen key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hlenCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        HashMap<String, String> hashMap = (HashMap<String, String>) redisObject.getValue();
        return MapUtils.isNotEmpty(hashMap) ? String.valueOf(hashMap.keySet().size()) : ResultCode.NO_OPS_SUCCEED.getMsg();
    }

    /**
     * hkeys key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hkeysCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        HashMap<String, String> hashMap = (HashMap<String, String>) redisObject.getValue();
        return String.join("\n", hashMap.keySet());
    }

    /**
     * hvals key
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String hvalsCommand(RedisClient redisClient, String[] argv) {
        String key = argv[1];
        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        HashMap<String, String> hashMap = (HashMap<String, String>) redisObject.getValue();
        return String.join("\n", hashMap.values());
    }

    /**
     * zdd key score field ...
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String zaddCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        String[] values = new String[argv.length - 2];
        if (values.length % 2 != 0) {
            return String.format(ResultCode.WRONG_NUMBER_ARFUMENTS.getMsg(), RedisCommand.ZADD.getCommandType());
        }
        System.arraycopy(argv, 2, values, 0, values.length);

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        dict.put(key, redisObjectService.getZSetObject(values));
        return String.valueOf(values.length / 2);
    }

    /**
     * zrank key field
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String zrankCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        String ele = argv[2];

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        ZSet zSet = (ZSet) redisObject.getValue();
        return String.valueOf(zSet.zrank(ele));

    }

    /**
     * zscore key field
     *
     * @param redisClient
     * @param argv
     * @return
     */
    public String zscoreCommand(RedisClient redisClient, String... argv) {
        String key = argv[1];
        String ele = argv[2];

        RedisDb redisDb = redisClient.getCurrentDb();
        ConcurrentHashMap<String, RedisObject> dict = redisDb.getDict();
        RedisObject redisObject = dict.get(key);
        if (Objects.isNull(redisObject)) {
            return ResultCode.NUll.getMsg();
        }
        ZSet zSet = (ZSet) redisObject.getValue();
        return zSet.zscore(ele);
    }

}
