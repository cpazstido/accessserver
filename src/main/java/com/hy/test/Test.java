package com.hy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Test {

	public static void main(String[] args) throws ParseException {
		byte []bTest={(byte)0x0A,(byte)0x02,(byte)0x00,(byte)0x59,(byte)0x4A,(byte)0x43,(byte)0x5F,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x04,(byte)0x04,(byte)0x80,(byte)0x01,(byte)0xFF};
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[0]&0xff));
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[1]&0xff));
		
		String sDay = "1970-01-01";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date d1 = sdf.parse(sDay);
		System.out.println(new Date().getTime()/1000);
	}
}
