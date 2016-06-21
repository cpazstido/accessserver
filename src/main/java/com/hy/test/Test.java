package com.hy.test;

import javax.swing.plaf.synth.SynthRadioButtonMenuItemUI;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class Test {
	private String name;
	private static final String base = " base string. ";
	private static final int count = 2000000;

	public static void main(String[] args) throws ParseException {
		byte []bTest={(byte)0x05,(byte)0x00,(byte)0x44,(byte)0x59,(byte)0x4A,(byte)0x43,(byte)0x5F,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x32,(byte)0x30,(byte)0x31,(byte)0x36,(byte)0x07,(byte)0xA4,(byte)0x01,(byte)0x00,(byte)0xA0,(byte)0xBD,(byte)0x63,(byte)0x57};
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[0]&0xff));
		System.out.println(Integer.toHexString(CRC16.getCrc(bTest)[1]&0xff));
		
		String sDay = "1970-01-01";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date d1 = sdf.parse(sDay);
		System.out.println(new Date().getTime()/1000);

		System.out.println("*******************************");
		System.out.println(ExceptionTest());
		System.out.println("*******************************");
	}

	public static String ExceptionTest(){
		try{
			System.out.println("try");
			Test test = null;
			System.out.println(test.name);
			return "return try";
		}catch (NullPointerException e){
			System.out.println("catch");
			return "return catch";
		}catch (Exception e){
			System.out.println("eee");
			return "return eee";
		}
		finally{
			System.out.println("finally");
			//return "return finally";
		}
	}
}
