package com.mini.redis.server.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/29 18:53
 */
public class ZSetUtil {

    /**
     * 跳表允许最大层级
     */
    public static final int ZSKIPLIST_MAXLEVEL = 32;
    /**
     * 跳表升层概率
     */
    private static final float ZSKIPLIST_P = 0.25f;

    /**
     * <p>
     * Returns a random level for the new skiplist node we are going to create.
     * The return value of this function is between 1 and ZSKIPLIST_MAXLEVEL
     * (both inclusive), with a powerlaw-alike distribution where higher
     * levels are less likely to be returned.
     *
     * @return level
     */
    public static int zslRandomLevel() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int level = 1;
        while (level < ZSKIPLIST_MAXLEVEL) {
            if (!(random.nextFloat() < ZSKIPLIST_P)) break;
            level++;
        }
        return level;
    }

    /**
     * 释放update引用的对象
     */
    public static void releaseUpdate(Object[] update, int realLength) {
        for (int index = 0; index < realLength; index++) {
            update[index] = null;
        }
    }

    /**
     * 重置rank中的数据
     */
    public static void releaseRank(int[] rank, int realLength) {
        for (int index = 0; index < realLength; index++) {
            rank[index] = 0;
        }
    }
}
