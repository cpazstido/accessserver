package com.hy.SimulateDevice;

public class test {
    public static void main(String[] args){	
        int i = Float.floatToIntBits((float)0.6 );
        byte[] b = intToBytes2(i);
		for(int j=0;j<b.length;j++){
		    System.out.println(b[j]&0xFF);
		}
		return;
    }	
    public static byte[] intToBytes2(int n) {  
        byte[] b = new byte[4];  
        for (int i = 0; i < 4; i++) {  
            b[i] = (byte) (n >> (24 - i * 8));  
        }  
        return b;  
    }
}
