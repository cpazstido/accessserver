package com.hy.SimulateDevice;

import java.nio.charset.Charset;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.math.BigDecimal;



public class CommonFunctions
{

    //世纪秒(是指1970年1月1日0时0分到指定时间过去的秒数)转换为年月日，时分秒

    public static String centurySecondToDateTime(long  time)
    {
        Calendar ca = Calendar.getInstance();
        //Date d = ca.getTime();
        //long l = ca.getTimeInMillis();
        //ca.set(1970, 0, 1);
        //long L1970 = ca.getTimeInMillis();
        //ca.setTime(d);
        //ca.setTimeInMillis(l);

        String out = "";
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time * 1000);
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        out = sdformat.format(gc.getTime());

        return out;
    }

    //将十六进制字符串转换为double, 41AEF5C2
    public static float hexStrToFloat(String str)
    {
        float result = 0;
        try
        {
            int temp = Integer.parseInt(str.trim(), 16);
            result = Float.intBitsToFloat(temp);
        }
        catch (NumberFormatException e)
        {
            long ltemp = Long.parseLong(str.trim(), 16);
            //long ltemp = Integer.parseInt(str.trim(), 16);
            result = Float.intBitsToFloat((int)ltemp);
        }
        //只保留小数点后两位
        //result = (float)(Math.round(result*100))/100;
        return result;
    }


    //输入16进制字符串(如 5a5b5c)，输出相反顺序的16进制字符串(5c5b5a)。
    public static String  reverseOrder(String s)
    {

        char[] chA = s.toCharArray();
        int l = s.length();
        for(int i =0; i< l/2; i=i+2)
        {
            char cTmp1= 0;
            char cTmp2= 0;
            cTmp1 = chA[i];
            cTmp2 = chA[i+1];
            chA[i] = chA[l-i-2];
            chA[i+1] = chA[l-i-1];
            chA[l-i-2] = cTmp1;
            chA[l-i-1] = cTmp2;
        }
        String sRet  = new String(chA);
        return sRet;
    }
    /**
     * 字符串转换成十六进制字符串
     * @param str 待转换的ASCII字符串
     * @return String 如: [616C6B]
     */
    public static String strToHexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            //sb.append(' ');
        }
        return sb.toString().trim();
    }



    /**
     * 十六进制转换字符串
     * @param hexStr 字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStrToStr(String hexStr)
    {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
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

    /**
     * bytes字符串转换为Byte值
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStrToBytes(String src)
    {
        int m=0,n=0;
        int cc = src.length();
        if(cc%2 != 0){
            //logger.debug("函数 hexStrToBytes 输入的字符为奇数，这有可能会有问题,输入的字符个数是: " + cc);
        }
        int l=src.length()/2;
        byte[] ret = new byte[l];
        String sSub ;
        for (int i = 0; i < l; i++)
        {
            sSub = src.substring(i*2,i*2+2);
            ret[i] = (byte)( Integer.parseInt(sSub, 16) );

        	/*
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
            */
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     * @param strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception
    {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++)
        {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * 截取指定字节数组
     * @param src    源字节数组
     * @param begin    起始下标
     * @param count    截取字节个数
     * @return    新的字节数组
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }

    /**
     * 合并两个byte数组
     * @param pByteA
     * @param pByteB
     * @return
     */
    public static byte[] getMergeBytes(byte[] pByteA, byte[] pByteB){
        int aCount = pByteA.length;
        int bCount = pByteB.length;
        byte[] b = new byte[aCount + bCount];
        for(int i=0;i<aCount;i++){
            b[i] = pByteA[i];
        }
        for(int i=0;i<bCount;i++){
            b[aCount + i] = pByteB[i];
        }
        return b;
    }


    /**
     * 字符转换为日期类型
     * @param dateString
     * @return
     */
    public static Date parseDateTime(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date result = null;
        try {
            result = df.parse(dateString);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 两个日期相减计算多少天
     * @param firstDate
     * @param lastDate
     * @return A double days
     */
    public static int compareDateToDays(Date firstDate, Date lastDate) {
        if (firstDate == null || lastDate == null) {
            System.out.print("NULL");
        }
        long time1 = firstDate.getTime();
        long time2 = lastDate.getTime();
        long tmpCal = time2 - time1;
        long mm = 24 * 60 * 60 * 1000;
        int days = (int) (tmpCal / mm);
        return Math.abs(days);
    }
    public static float getFloatFromBytes(byte[] bCommand, int startPos){
        int l;
        l = bCommand[startPos + 0];
        l &= 0xff;
        l |= ((long) bCommand[startPos + 1] << 8);
        l &= 0xffff;
        l |= ((long) bCommand[startPos + 2] << 16);
        l &= 0xffffff;
        l |= ((long) bCommand[startPos + 3] << 24);
        float fRet = Float.intBitsToFloat(l);
        fRet = (float)(Math.round(fRet*100))/100;
        return fRet;
    }

    public static long getLongFromBytes(byte[] bCommand, int startPos){
        long l;
        l = bCommand[startPos + 0];
        l &= 0xff;
        l |= ((long) bCommand[startPos + 1] << 8);
        l &= 0xffff;
        l |= ((long) bCommand[startPos + 2] << 16);
        l &= 0xffffff;
        l |= ((long) bCommand[startPos + 3] << 24);
        return l;
    }

}
