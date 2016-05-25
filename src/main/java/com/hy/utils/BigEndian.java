package com.hy.utils;

/**
 * Created by cpazstido on 2016/5/24.
 */
public class BigEndian {
    public static int getBigEndianInt(byte[] bytes){
        int b0 = bytes[0] & 0xFF;
        int b1 = bytes[1] & 0xFF;
        int b2 = bytes[2] & 0xFF;
        int b3 = bytes[3] & 0xFF;

        return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
    }

    public static byte[] toBigEndian(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i >>> 24);
        bytes[1] = (byte) (i >>> 16);
        bytes[2] = (byte) (i >>> 8);
        bytes[3] = (byte) i;
        return bytes;
    }
}
