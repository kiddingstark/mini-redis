package com.mini.redis.server.struct.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/23 21:59
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListNode {

    private ListNode prev;

    private ListNode next;

    private Object value;
}
