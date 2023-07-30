package com.mini.redis.server.struct.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author: zhengruihong
 * @description 模拟Redis压缩列表,考虑到Integer对象包含对象头+实例数据+对齐填充，远远大于int 4个字节，数据类型禁用包装类型
 * @date: 2023/7/11 10:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZipList implements Serializable {

    /**
     * The general layout of the ziplist is as follows:
     *
     * <zlbytes> <zltail> <zllen> <entry> <entry> ... <entry> <zlend>
     */

    private long id;

    /**
     * an integer to hold the number of bytes that
     * the ziplist occupies, including the four bytes of the zlbytes field itself.
     * This value needs to be stored to be able to resize the entire structure
     * without the need to traverse it first.
     */
    private int zlbytes;

    /**
     * the offset to the last entry in the list. This allows
     * a pop operation on the far side of the list without the need for full traversal.
     */
    private int zltail;

    /**
     * the number of entries
     * redis中为16 bytes
     */
    private int zllen;

    /**
     * <prevlen from 0 to 253> <encoding> <entry>
     * 0xFE <4 bytes unsigned little endian prevlen> <encoding> <entry>
     */
    private List<String> entries;

    /**
     * a special entry representing the end of the ziplist.
     * redis中为8 bytes
     */
    private int zlend;

}
