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

    public static String byteToHexStr(byte[] b, int length)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            //sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }
}
