package com.hy.SimulateDevice;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.Logger;



public class UDP  {
	static Logger logger = Logger.getLogger(Simulate.class);
	private DatagramSocket dSocket;
	protected DatagramPacket datagramPacket;
	protected int iRcvBufEnd;
	protected byte[] bRcvBuf; //内存中的一个接收命令缓存区

	public UDP()  {

	}

	public UDP(int port) throws SocketException {
		logger.debug("这里是UDP的构造函数");
		byte[] buf=new byte[1024];
		datagramPacket=new DatagramPacket(buf,1024);
		this.dSocket = new DatagramSocket(port);
	}


	public void recv() throws Exception{
		String sTmp = "";
		if(dSocket != null && datagramPacket != null){
			logger.debug("UdpRecv 运行正常");
		}
		while(true){
			dSocket.receive(datagramPacket);

			byte[] bReadNew = CommonFunctions.subBytes(datagramPacket.getData(), 0, datagramPacket.getLength());
			int len = bReadNew.length;
			System.arraycopy(bReadNew, 0, bRcvBuf, iRcvBufEnd, len);
			iRcvBufEnd = iRcvBufEnd + len;

			//this.getCommand(); //调用基类的函数进行处理

		}
	}

	public void send(String sDeviceID, byte[] command, String ip, int port) throws IOException{
		dSocket.send(new DatagramPacket(command, command.length,
				InetAddress.getByName(ip),port));
	}
}
