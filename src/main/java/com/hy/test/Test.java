package com.hy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Test {	

	public static void main(String[] args) throws ParseException {
		byte []bTest={(byte)04,(byte)0x00,(byte)0x48,(byte)0x59,(byte)0x5F,(byte)0x4F,(byte)0x4C,(byte)0x4D,(byte)0x53,(byte)0x5F,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x35,(byte)0x33,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0xAD,(byte)0x63,(byte)0x0B,(byte)0x57};
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[0]&0xff));
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[1]&0xff));
		
		String sDay = "1970-01-01";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date d1 = sdf.parse(sDay);
		System.out.println(new Date().getTime()/1000);		
	}
}
