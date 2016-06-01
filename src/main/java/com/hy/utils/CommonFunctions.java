//常用函数

package com.hy.utils;


import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.hy.bean.Header;
import com.hy.bean.NettyMessage;
import org.apache.log4j.Logger;

public class CommonFunctions
{
    public static Logger logger = Logger.getLogger(CommonFunctions.class);

	//世纪秒(是指1970年1月1日0时0分到指定时间过去的秒数)转换为年月日，时分秒

    public static NettyMessage bytesToNettyMessage(byte []in){
        byte[] bytesFlag = new byte[4];
        bytesFlag = ByteHelper.subBytes(in, 0, 4);
        byte[] bytesVersion = new byte[1];
        bytesVersion = ByteHelper.subBytes(in, 4, 1);
        byte[] bytesIndex = new byte[4];
        bytesIndex = ByteHelper.subBytes(in, 5, 4);
        byte[] bytesType = new byte[1];
        bytesType = ByteHelper.subBytes(in, 9, 1);
        byte[] bytesLength = new byte[4];
        bytesLength = ByteHelper.subBytes(in, 10, 4);
        int bodyLength = BigEndian.getBigEndianInt(bytesLength);
        byte []msgBodyBuf = new byte[bodyLength];

        byte[] dataBytes = new byte[bodyLength + 14];
        System.arraycopy(bytesFlag, 0, dataBytes, 0, 4);
        System.arraycopy(bytesVersion, 0, dataBytes, 4, 1);
        System.arraycopy(bytesIndex, 0, dataBytes, 5, 4);
        System.arraycopy(bytesType, 0, dataBytes, 9, 1);
        System.arraycopy(bytesLength, 0, dataBytes, 10, 4);
        System.arraycopy(msgBodyBuf, 0, dataBytes, 14, bodyLength);

        NettyMessage message = new NettyMessage();
        Header header = new Header();

        header.setFlag(bytesFlag);
        header.setVersion(bytesVersion[0]);
        header.setIndex(BigEndian.getBigEndianInt(bytesIndex));
        header.setTypes(bytesType[0]);
        header.setLen(BigEndian.getBigEndianInt(bytesLength));
        message.setHeader(header);
        message.setBody(msgBodyBuf);
        return message;
    }

    public static byte[] nettyMessageToBytes(NettyMessage nettyMessage){
        int bodyLength = nettyMessage.getHeader().getLen();

        byte mes[] = new byte[14+bodyLength];
        System.arraycopy(nettyMessage.getHeader().getFlag(),0,mes,0,4);
        byte version[] = new byte[1];
        version[0] = nettyMessage.getHeader().getVersion();
        System.arraycopy(version,0,mes,4,1);
        System.arraycopy(BigEndian.toBigEndian(nettyMessage.getHeader().getIndex()),0,mes,5,4);
        byte types[] = new byte[1];
        types[0] = nettyMessage.getHeader().getTypes();
        System.arraycopy(types,0,mes,9,1);
        System.arraycopy(BigEndian.toBigEndian(nettyMessage.getHeader().getLen()),0,mes,10,4);
        if(bodyLength != 0){
            byte []bb = (byte [])nettyMessage.getBody();
            System.arraycopy(bb, 0, mes, 14, bodyLength);
        }

        return mes;
    }

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
     * @param hexStr(Byte之间无分隔符 如:[616C6B])
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
     * @param  b byte数组
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
            logger.debug("函数 hexStrToBytes 输入的字符为奇数，这有可能会有问题,输入的字符个数是: " + cc);
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
     * unicode的String转换成String的字符串
     * @param hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex)
    {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++)
        {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
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
    /** 校验Ip格式 这是模仿js校验ip格式，使用java做的判断 
     * @param str 
     * @return 
     */  
    public static boolean checkIp(String str)  
    {  
    	if(str == null){
    		return false;
    	}
        String[] ipValue = str.split("\\.");  
        if (ipValue.length != 4)  
        {  
            return false;  
        }  
        for (int i = 0; i < ipValue.length; i++)  
        {  
            String temp = ipValue[i];  
            try  
            {  
                // java判断字串是否整数可以用此类型转换异常捕获方法，也可以用正则 var regu = /^\d+$/;  
                Integer q = Integer.valueOf(ipValue[i]);  
                if (q > 255)  
                {  
                    return false;  
                }  
            }  
            catch (Exception e)  
            {  
                return false;  
            }  
        }  
        return true;  
    }
    
    public static int byteToInt(byte[] b) {      
      int result = 0;   
      int i0 = b[0]&0xFF;
      int i1 = (b[1]&0xFF)<<8;
      int i2 = (b[2]&0xFF)<<16;
      int i3 = (b[3]&0xFF)<<24;
      
      result = i0+i1+i2+i3;      
      return result;   
    }
    
    public static byte[] intToByteArray(int i) {   
	  byte[] result = new byte[4];   
	  result[3] = (byte)((i >>> 24));
	//必须把我们要的值弄到最低位去，有人说不移位这样做也可以， result[0] = (byte)(i  & 0xFF000000);
	//，这样虽然把第一个字节取出来了，但是若直接转换为byte类型，会超出byte的界限，出现error。再提下数//之间转换的原则（不管两种类型的字节大小是否一样，原则是不改变值，内存内容可能会变，比如int转为//float肯定会变）所以此时的int转为byte会越界，只有int的前三个字节都为0的时候转byte才不会越界。虽//然 result[0] = (byte)(i  & 0xFF000000); 这样不行，但是我们可以这样 result[0] = (byte)((i  & //0xFF000000) >>24);
	  result[2] = (byte)((i >>> 16));
	  result[1] = (byte)((i >>> 8));
	  result[0] = (byte)(i);
	  return result;
   } 
    
    public static int getIntFromBytes(byte[] bCommand, int startPos){
    	int l;
        l = bCommand[startPos + 0];
        l &= 0xff;
        l |= ((int) bCommand[startPos + 1] << 8);  
        l &= 0xffff;
        return l;
    }  
    /**
     *字符串度分秒转换为度 
     */
    public static Float degreeMinutesConversion(String latlng) {
        float du = 0;
        float fen = 0;
        float miao = 0;
        float s=0;
        if(latlng.indexOf("度")!=-1){
            du = Float.parseFloat(latlng.substring(0, latlng.indexOf("度")));
            System.out.println(du);
            if(latlng.indexOf("分")!=-1){
                fen = Float.parseFloat(latlng.substring(latlng.indexOf("度") + 1, latlng.indexOf("分")));
               System.out.println(fen);
                if(latlng.indexOf("秒")!=-1){
                    miao = Float.parseFloat(latlng.substring(latlng.indexOf("分") + 1, latlng.indexOf("秒")));
                }
            }
            System.out.println(miao);
        }
        if (du < 0){
        	s= (float) -(Math.abs(du) + (fen + (miao / 60.000)) / 60.000);
        	}
            s=(float) (du + (fen + (miao / 60.000)) / 60.000); 
        System.out.println(latlng+" string");
        System.out.println(du);
        System.out.println(fen);
        System.out.println(miao);
        System.out.println(s+"  float");
        return  s;

    }
    /**
     * 将小数度数转换为度分秒格式
     * @param 
     * @return
     */
    public static String convertToSexagesimal(float num) {  
    	String []duStrings=(num+"").split("\\."); 
    	int du=Integer.parseInt(duStrings[0]);
    	float temp =(num-du)*60;	
        String []fenString=(temp+"").split("\\."); 
        int fen =Integer.parseInt(fenString[0]); 
        double miao =(temp-fen)* 60;
        miao=(Math.round(miao*1000))/1000.000;
        if (num < 0)
            return "-" + du + "度" + fen + "分" + miao + "秒";

        return du + "度" + fen + "分" + miao + "秒";

    }
    // 获取小数部分
    public static double getdPoint(double num) {
        double d = num;
        int fInt = (int) d;
        BigDecimal b1 = new BigDecimal(Double.toString(d));
        BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
        double dPoint = b1.subtract(b2).floatValue();
        return dPoint;
        
    }
    
  /**long类型转成byte数组 
   * 
   * @param number
   * @return
   */
    public static byte[] longToByte(long number) { 
          long temp = number; 
          byte[] b = new byte[8]; 
          for (int i = 0; i < b.length; i++) { 
              b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位 
              temp = temp >> 8; // 向右移8位 
          } 
          return b; 
      }
    /**
     * int转byte
     * @param i
     * @return
     */
    public byte[] intToByte(int i) {
        byte[] abyte0 = new byte[4];
        abyte0[0] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[2] = (byte) ((0xff0000 & i) >> 16);
        abyte0[3] = (byte) ((0xff000000 & i) >> 24);
        return abyte0;
    }
	//byte数组转换成char数组
	private char[] getChars (byte[] bytes) {
		Charset cs = Charset.forName ("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (bytes.length);
		bb.put (bytes);
		bb.flip ();
		CharBuffer cb = cs.decode (bb);
		logger.info("in size=" + bytes.length + "out size = " + cb.array().length);
		return cb.array();
	}
	//char数组转换成byte数组
	private byte[] getBytes (char[] chars) {
	   Charset cs = Charset.forName ("UTF-8");
	   CharBuffer cb = CharBuffer.allocate (chars.length);
	   cb.put (chars);
	   cb.flip ();
	   ByteBuffer bb = cs.encode (cb);
	   return bb.array();
	}
	public  byte[] hexStringToBytes(String hexString) {
	    if (hexString == null || hexString.equals("")) {
	        return null;
	    }
	    hexString = hexString.toUpperCase();
	    int length = hexString.length() / 2;
	    char[] hexChars = hexString.toCharArray();
	    byte[] d = new byte[length];
	    for (int i = 0; i < length; i++) {
	        int pos = i * 2;
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
	    }
	    return d;
	}
	  public  byte charToByte(char c) {
	        return (byte) "0123456789ABCDEF".indexOf(c);
	    }
}
