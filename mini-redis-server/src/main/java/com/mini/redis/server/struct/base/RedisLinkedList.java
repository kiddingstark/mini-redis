package com.mini.redis.server.struct.base;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author: zhengruihong
 * @description 模拟adlist.h中的list
 * @date: 2023/7/23 21:58
 */
@Builder
@Data
public class RedisLinkedList<E> extends LinkedList<E>{

    public RedisLinkedList(){
        super();
    }

    public RedisLinkedList(Collection<? extends E> c) {
        super();
    }


    public E rPop() {
        return removeLast();
    }

}
