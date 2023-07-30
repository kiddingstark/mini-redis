package com.mini.redis.server.struct.base;

import com.mini.redis.server.constant.ResultCode;
import com.mini.redis.server.util.ZSetUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengruihong
 * @description 模拟server.h中的zset
 * @date: 2023/7/29 17:51
 */
@Builder
@Data
@AllArgsConstructor
public class ZSet implements Serializable {

    public ZSet() {
        this.dict = new ConcurrentHashMap<>();
        this.zsl = new ZSkipList();
    }

    private ConcurrentHashMap<String, Double> dict;

    private ZSkipList zsl;

    public void zadd(double score, String ele) {
        Objects.requireNonNull(ele);
        dict.put(ele, score);
        zsl.zslInsert(score, ele);
    }

    public int zrank(String ele) {
        if (StringUtils.isBlank(ele)) {
            return -1;
        }
        Double score = dict.get(ele);
        if (Objects.isNull(score)) {
            return -1;
        }
        // 0 < zslGetRank <= size
        return zsl.zslGetRank(score, ele) - 1;
    }

    public String zscore(String ele) {
        if (StringUtils.isBlank(ele)) {
            return ResultCode.NUll.getMsg();
        }
        Double score = dict.get(ele);
        if (Objects.isNull(score)) {
            return ResultCode.NUll.getMsg();
        }
        return String.valueOf(score);
    }

    public static class ZSkipList implements Serializable {

        public ZSkipList() {
            this.head = zslCreateNode(null, 0, ZSetUtil.ZSKIPLIST_MAXLEVEL);
        }

        private ZSkipListNode head;

        private ZSkipListNode tail;

        private int level = 1;

        /**
         * head不包括在内
         */
        private int length = 0;

        public static class ZSkipListNode implements Serializable {

            public ZSkipListNode(String ele, double score, ZSkipListNodeLevel[] level) {
                this.ele = ele;
                this.score = score;
                this.level = level;
            }

            /**
             * 值，实际上是一个sds object
             */
            private final String ele;

            /**
             * 分数
             */
            private final double score;

            private ZSkipListNode backward;

            private final ZSkipListNodeLevel[] level;


            /**
             * 层
             */
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            static class ZSkipListNodeLevel {

                /**
                 * 前进指针，相当于next node
                 */
                private ZSkipListNode forward;

                /**
                 * 跨度
                 */
                private int span;
            }
        }

        private final ZSkipListNode[] updateCache = new ZSkipListNode[ZSetUtil.ZSKIPLIST_MAXLEVEL];
        private final int[] rankCache = new int[ZSetUtil.ZSKIPLIST_MAXLEVEL];

        public void zslInsert(double score, String ele) {
            final int level = ZSetUtil.zslRandomLevel();
            final ZSkipListNode[] update = updateCache;
            final int[] rank = rankCache;
            final int realLength = Math.max(level, this.level);
            try {
                //要插入节点的preNode:x
                ZSkipListNode x = head;
                for (int i = this.level - 1; i >= 0; i--) {
                    rank[i] = i == (this.length - 1) ? 0 : rank[i + 1];
                    //有forward node,并forward node的分数/值比要插入的node小
                    while (Objects.nonNull(x.level[i].forward) &&
                            (x.level[i].forward.score < score ||
                                    (x.level[i].forward.score == score && ele.compareTo(x.level[i].forward.ele) > 0))) {
                        rank[i] += x.level[i].span;
                        x = x.level[i].forward;
                    }
                    update[i] = x;
                }

                if (level > this.level) {
                    /* 新节点的层级大于当前层级，那么高出来的层级导致需要更新head，且排名和跨度是固定的 */
                    for (int i = this.level; i < level; i++) {
                        rank[i] = 0;
                        update[i] = head;
                        update[i].level[i].span = this.length;
                    }
                    this.level = level;
                }

                /* we assume the key is not already inside, since we allow duplicated
                 * scores, and the re-insertion of score and redis object should never
                 * happen since the caller of zslInsert() should test in the hash table
                 * if the element is already inside or not.*/
                final ZSkipListNode newNode = this.zslCreateNode(ele, score, level);

                /* 这些节点的高度小于等于新插入的节点的高度，需要更新指针。此外它们当前的跨度被拆分了两部分，需要重新计算。 */
                for (int i = 0; i < level; i++) {
                    /* 链接新插入的节点 */
                    newNode.level[i].forward = update[i].level[i].forward;
                    update[i].level[i].forward = newNode;

                    /* update span covered by update[i] as newNode is inserted here */
                    newNode.level[i].span = update[i].level[i].span - (rank[0] - rank[i]);
                    update[i].level[i].span = (rank[0] - rank[i]) + 1;
                }

                /* increment span for untouched levels */
                for (int i = level; i < this.level; i++) {
                    update[i].level[i].span++;
                }

                /* 设置新节点的前向节点(回溯节点) - 这里不包含header，一定注意 */
                newNode.backward = (update[0] == this.head) ? null : update[0];

                /* 设置新节点的后向节点 */
                if (newNode.level[0].forward != null) {
                    newNode.level[0].forward.backward = newNode;
                } else {
                    this.tail = newNode;
                }

                this.length++;

            } finally {
                ZSetUtil.releaseUpdate(update, realLength);
                ZSetUtil.releaseRank(rank, realLength);
            }
        }

        int zslGetRank(double score, String ele) {
            int rank = 0;
            ZSkipListNode x = this.head;
            for (int i = this.level - 1; i >= 0; i--) {
                //等于的情况下继续前进，也就是说在目标节点停下来
                while (Objects.nonNull(x.level[i].forward) &&
                        (x.level[i].forward.score < score ||
                                (x.level[i].forward.score == score && ele.compareTo(x.level[i].forward.ele) >= 0))) {
                    rank += x.level[i].span;
                    x = x.level[i].forward;
                }

                /* x might be equal to zsl->header, so test if obj is non-NULL */
                if (x != this.head && ele.compareTo(x.ele) == 0) {
                    return rank;
                }
            }
            return 0;
        }


        /**
         * create ZSkipListNode
         *
         * @param level
         * @param score
         * @param ele
         * @return
         */
        private ZSkipListNode zslCreateNode(String ele, double score, int level) {
            final ZSkipListNode node = new ZSkipListNode(ele, score, new ZSkipListNode.ZSkipListNodeLevel[level]);
            for (int index = 0; index < level; index++) {
                node.level[index] = new ZSkipListNode.ZSkipListNodeLevel();
            }
            return node;
        }
    }
}
