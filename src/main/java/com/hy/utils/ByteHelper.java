package com.hy.utils;

/**
 * Created by cpazstido on 2016/5/11.
 */
public class ByteHelper {
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
}
