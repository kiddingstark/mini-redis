package com.mini.redis.server.struct.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/13 22:08
 */
@Data
@Builder
@AllArgsConstructor
public class IntSet {

    //encoding为16位、32位、64位的int值选择
    //private Integer encoding;

    private int length;

    private int[] contents;

    public IntSet() {
        this.length = 0;
        this.contents = new int[0];
    }

    /**
     * @param value
     */
    public void intSetAdd(int value) {
        int[] contents = this.getContents();
        AtomicInteger pos = new AtomicInteger();
        int exist = this.intSetSearch(value, pos);
        if (exist == 1) {
            return;
        }
        int[] newContent = new int[contents.length + 1];
        if (pos.get() >= 0) System.arraycopy(contents, 0, newContent, 0, pos.get());
        newContent[pos.get()] = value;
        if (newContent.length - (pos.get() + 1) >= 0)
            System.arraycopy(contents, pos.get() + 1 - 1, newContent, pos.get() + 1, newContent.length - (pos.get() + 1));
        this.setContents(newContent);
    }

    /**
     * Search for the position of "value".
     * Return 1/0
     *
     * @param value
     * @param pos
     */
    public int intSetSearch(Integer value, AtomicInteger pos) {
        int[] contents = this.getContents();
        if (Objects.isNull(contents) || contents.length == 0) {
            pos.set(0);
            return 0;
        }
        int min = 0;
        int max = contents.length - 1;
        int indexValue = -1;
        //二分
        while (max >= min) {
            int index = (max + min) / 2;
            indexValue = contents[index];
            if (value > indexValue) {
                min = index + 1;
            } else if (value < indexValue) {
                max = index - 1;
            } else {
                break;
            }
        }
        if (value == indexValue) {
            return 1;
        }
        pos.set(min);
        return 0;
    }

    /**
     * intSetPop
     *
     * @return
     */
    public String intSetPop() {
        int[] contents = this.getContents();
        if (Objects.isNull(contents) || contents.length == 0) {
            return null;
        }
        int length = this.getLength();
        int index = (int) (Math.random() * (length - 1));

        int[] newContent = new int[contents.length - 1];
        System.arraycopy(contents, 0, newContent, 0, index);
        if (contents.length - 1 - index >= 0)
            System.arraycopy(contents, index + 1, newContent, index, contents.length - 1 - index);
        this.setContents(newContent);
        return Integer.toString(contents[index]);
    }

}
