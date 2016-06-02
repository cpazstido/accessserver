package com.hy.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;



/*
通信基类，里面包含了串口、udp、tcp通信都会用到的基本函数
*/

public class SocketCommFunc {
	protected static Logger logger = Logger.getLogger(SocketCommFunc.class);
	
	public SocketCommFunc(){

	}
	
	/*socket 发送消息函数*/
	public static int send(BufferedOutputStream outputStream, byte[] sendbuffer)
	{
		 if(outputStream!=null){
			  try {
				  outputStream.write(sendbuffer);
				  outputStream.flush();
			    return 1;
			  } catch (IOException ioe) {
			    logger.error( "TCPMessageSocket::send(): send() failed: " + ioe);
			    ioe.printStackTrace();
			    return -1;
			  }	 
		 }else{
			 logger.debug("向接入服务器发送消息失败,socket未连接");
		 }
		return 0;
	}
	  
	  /*socket 发送消息函数*/
	  public static int send(BufferedOutputStream outputStream, String sendbuffer, int length)
	  {
	    try {
	      outputStream.write(sendbuffer.getBytes());
	      outputStream.flush();
	      return 1;
	    } catch (IOException ioe) {
	      logger.error( "TCPMessageSocket::send(): send() failed: " + ioe);
	      return -1;
	    }
	    
	  }
	  
	  /*socket 发送文件的函数*/
	  public static int send(BufferedOutputStream outputStream, String fileName)
	  {
		  try{
			 InputStream inputStreamFromFile=new FileInputStream(new File(fileName));
			 byte[] bs=new byte[1024]; 
			 int len=0;
			 while((len = inputStreamFromFile.read(bs)) != -1){
				 outputStream.write(bs,0,len);
			 }
			 outputStream.flush();
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  return 0;
	  }
	  
	  
	  /*socket 接收消息函数*/
	  public static int receive(BufferedInputStream inputStream, byte[] recvbuffer, int maxlength)
	  {
	    int numbytes;
	    try {
	      numbytes = inputStream.read(recvbuffer, 0, maxlength );
	    } catch (IOException ioe) {
	      return -1;
	    }
	    return numbytes;
	  }
	  
	  /*socket 接收消息函数*/
	  public static String receive(BufferedInputStream inputStream)
	  {
		  String ss="";
		  try {
			  byte[] buf=new byte[inputStream.available()]; 
			  logger.debug("inputStream.avialable==="+inputStream.available());
				  inputStream.read(buf);
				  ss+=new String( buf);
			  logger.debug("ss======\\r\\n"+ss);
			  return ss;
		  } catch (IOException ioe) {
			  logger.error( "TCPMessageSocket::recieve(): recv() failed: " + ioe );
			  ioe.printStackTrace();
			  return "";
		  }
		  
	  }
	  
	  /*socket 关闭函数*/
	  public static void close(Socket socket) {
		try {
			socket.close();
		} catch (IOException ioe) {
		    logger.error( "Error closing TCP socket: " + ioe );
		}
	  }
	  
	  /*对输入流正确行进行校验*/
	  public static boolean available(BufferedInputStream inputStream) {
		  try {
				int i = inputStream.available();
				System.out.println("校验返回 i= " + i);
				return true;
			} catch (IOException e) {
				System.out.println("available 校验无效");
				return false;
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}	  
	  }	  
	    
}




