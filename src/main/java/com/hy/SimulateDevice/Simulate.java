package com.hy.SimulateDevice;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Simulate {
	static Logger logger = Logger.getLogger(Simulate.class);
	static String m_IP ;
	static UDP udp ;

	static int m_DeviceNum;

	//待测设备列表
	static List m_DeviceList = new ArrayList();

	//离线设备列表
	static List m_OflineList = new ArrayList();

	Simulate(){
		m_DeviceNum = 0;
		m_IP = "";
		try {
			udp = new UDP(9001);
		} catch (SocketException e) {
			// TODO A50uto-generated catch block
			e.printStackTrace();
		}
	}

	//向待测试列表中添加所有的设备编号从Device_0000000001开始
	public static void main(String[] args){
		Simulate simulate = new Simulate();
		do{
			logger.debug("请输入模拟的设备数量，数量在1~1000之间，命令格式如下：devicenum 5 ");
			Scanner sc = new Scanner(System.in);
			m_DeviceNum = sc.nextInt();
		}while(m_DeviceNum<=0||m_DeviceNum>1000);

		do{
			logger.debug("请输入正确的IP地址，命令格式如下：ip 172.16.8.123 ");
			Scanner sc = new Scanner(System.in);
			m_IP = sc.nextLine();
		}while(!isboolIP(m_IP));

		//创建设备列表
		for(int i=0;i<m_DeviceNum;i++){
			String sI = String.valueOf(i+1);
			String deviceID = "Device_" ;
			for(int j=0;j<(10-sI.length());j++){
				deviceID = deviceID + "0";
			}
			deviceID = deviceID + sI;
			m_DeviceList.add(deviceID);
		}

		//启动一个定时器，每隔2分钟，所有设备向固定IP和端口发送数据。
		Timer timer1 = new Timer();
		timer1.schedule(new SentDataTask(), 1000, 10000);

		//循环监测用户输入的命令
		while(true){
			Scanner sc = new Scanner(System.in);
			String s1 = sc.nextLine();

			String[] cmd = s1.split("\\s+");
			if(cmd[0].equals("offlineDev")){
				logger.debug("设置设备离线列表");
				continue;
			}else if(cmd.equals("onlineDev")){
				logger.debug("设置设备上线列表");
				continue;
			}else{
				logger.debug("输入设备离线列表，命令格式如下：offlineDev Device_0000000008 Device_0000000010");
				logger.debug("输入设备上线列表，命令格式如下：onlineDev Device_0000000008 Device_0000000010");
			}
		}
	}


	private static boolean isboolIP(String ipAddress){
		String	ip="(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\."
				+ "(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	private static  void sendCommand(String sDeviceID, byte[] bCommand) throws IOException
	{
		//填充通用的部分
		if(sDeviceID.getBytes().length!=17){
			logger.error("deviceId 不正确");
			return;
		}
		bCommand[0] = (byte)(0xA5&0xFF);//报文头低位
		bCommand[1] = (byte)(0x5A&0xFF);//报文头高位
		int iLen = bCommand.length -23;
		bCommand[2] = (byte)((iLen%255)&0xFF);//报文长度低位,报文长度包括报文类型之后，Crc校验之前的byte数
		bCommand[3] = (byte)((iLen>>8)&0xFF);//报文长度高位
		System.arraycopy(sDeviceID.getBytes(), 0, bCommand, 4, 17);

		//
		byte[] crc = CRC16.getCrc(bCommand);
		byte[] bCommandNew = CommonFunctions.getMergeBytes(bCommand, crc);
		logger.debug("发送命令的设备ID： "+sDeviceID);
		logger.info("发送的命令是:" + CommonFunctions.byteToHexStr(bCommandNew, bCommandNew.length));

		udp.send(sDeviceID, bCommandNew, m_IP, 7116);
	}


	//设备上传气象数据报
	private static  void setMeteorologyDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[92];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x01&0xFF); //报文类型，气象是0x01

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(53&0xFF); //平均风速
		bMessage[47] = (byte)(53&0xFF);
		bMessage[48] = (byte)(25&0xFF);
		bMessage[49] = (byte)(63&0xFF);

		bMessage[50] = (byte)(60&0xFF); //平均风向
		bMessage[51] = (byte)(0&0xFF);

		bMessage[52] = (byte)(53&0xFF); //最大风速
		bMessage[53] = (byte)(53&0xFF);
		bMessage[54] = (byte)(25&0xFF);
		bMessage[55] = (byte)(63&0xFF);

		bMessage[56] = (byte)(53&0xFF); //极大风速
		bMessage[57] = (byte)(53&0xFF);
		bMessage[58] = (byte)(25&0xFF);
		bMessage[59] = (byte)(63&0xFF);

		bMessage[60] = (byte)(53&0xFF); //标准风速
		bMessage[61] = (byte)(53&0xFF);
		bMessage[62] = (byte)(25&0xFF);
		bMessage[63] = (byte)(63&0xFF);

		bMessage[64] = (byte)(53&0xFF); //气温
		bMessage[65] = (byte)(53&0xFF);
		bMessage[66] = (byte)(25&0xFF);
		bMessage[67] = (byte)(63&0xFF);

		bMessage[68] = (byte)(60&0xFF); //湿度
		bMessage[69] = (byte)(0&0xFF);

		bMessage[70] = (byte)(53&0xFF); //气压
		bMessage[71] = (byte)(53&0xFF);
		bMessage[72] = (byte)(25&0xFF);
		bMessage[73] = (byte)(63&0xFF);

		bMessage[74] = (byte)(53&0xFF); //降雨量
		bMessage[75] = (byte)(53&0xFF);
		bMessage[76] = (byte)(25&0xFF);
		bMessage[77] = (byte)(63&0xFF);

		bMessage[78] = (byte)(53&0xFF); //降水强度
		bMessage[79] = (byte)(53&0xFF);
		bMessage[80] = (byte)(25&0xFF);
		bMessage[81] = (byte)(63&0xFF);

		bMessage[82] = (byte)(60&0xFF); //光辐射强度
		bMessage[83] = (byte)(0&0xFF);

		sendCommand(sDeviceID, bMessage);
	}


	//设备上传杆塔倾斜数据报
	private static  void setPoleTiltDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[74];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x0c&0xFF); //报文类型，杆塔倾斜是0x0c

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(0&0xFF); //倾斜度
		bMessage[47] = (byte)(0&0xFF);
		bMessage[48] = (byte)(0&0xFF);
		bMessage[49] = (byte)(0&0xFF);

		bMessage[50] = (byte)(0&0xFF); //顺线倾斜度
		bMessage[51] = (byte)(0&0xFF);
		bMessage[52] = (byte)(0&0xFF);
		bMessage[53] = (byte)(0&0xFF);

		bMessage[54] = (byte)(0&0xFF); //横向倾斜度
		bMessage[55] = (byte)(0&0xFF);
		bMessage[56] = (byte)(0&0xFF);
		bMessage[57] = (byte)(0&0xFF);

		bMessage[58] = (byte)(53&0xFF); //顺线倾斜角
		bMessage[59] = (byte)(53&0xFF);
		bMessage[60] = (byte)(25&0xFF);
		bMessage[61] = (byte)(63&0xFF);

		bMessage[62] = (byte)(53&0xFF); //横向倾斜角
		bMessage[63] = (byte)(53&0xFF);
		bMessage[64] = (byte)(25&0xFF);
		bMessage[65] = (byte)(63&0xFF);


		sendCommand(sDeviceID, bMessage);
	}

	//设备上传微风振动数据报
	private static  void setAeolianVibrationDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[60];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x1e&0xFF); //报文类型，微风振动是0x1e

		int intValue=(int)(Math.random()*4+1);
		bMessage[39] = (byte)(intValue&0xFF); //sensorID最后一位

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(60&0xFF); //微风振动幅值
		bMessage[47] = (byte)(0&0xFF);

		bMessage[48] = (byte)(53&0xFF); //微风振动频率
		bMessage[49] = (byte)(53&0xFF);
		bMessage[50] = (byte)(25&0xFF);
		bMessage[51] = (byte)(63&0xFF);

		sendCommand(sDeviceID, bMessage);
	}

	//设备上传导线弧垂数据报
	private static  void setConductorSagDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[67];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x20&0xFF); //报文类型，导线弧垂是0x20

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(0&0xFF); //导线弧垂
		bMessage[47] = (byte)(0&0xFF);
		bMessage[48] = (byte)(0&0xFF);
		bMessage[49] = (byte)(0&0xFF);

		bMessage[50] = (byte)(0&0xFF); //导线对地距离
		bMessage[51] = (byte)(0&0xFF);
		bMessage[52] = (byte)(0&0xFF);
		bMessage[53] = (byte)(0&0xFF);

		bMessage[54] = (byte)(53&0xFF); //导线切线与水平线夹角
		bMessage[55] = (byte)(53&0xFF);
		bMessage[56] = (byte)(25&0xFF);
		bMessage[57] = (byte)(63&0xFF);

		bMessage[58] = (byte)(0x01&0xFF);//测量法标示

		sendCommand(sDeviceID, bMessage);
	}

	//设备上传导线风偏数据报
	private static  void setWindageYawDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[66];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x23&0xFF); //报文类型，导线风偏是0x23

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(53&0xFF); //风偏角
		bMessage[47] = (byte)(53&0xFF);
		bMessage[48] = (byte)(25&0xFF);
		bMessage[49] = (byte)(63&0xFF);

		bMessage[50] = (byte)(53&0xFF); //倾斜角
		bMessage[51] = (byte)(53&0xFF);
		bMessage[52] = (byte)(25&0xFF);
		bMessage[53] = (byte)(63&0xFF);

		bMessage[54] = (byte)(0&0xFF); //最小电器间隙
		bMessage[55] = (byte)(0&0xFF);
		bMessage[56] = (byte)(0&0xFF);
		bMessage[57] = (byte)(0&0xFF);


		sendCommand(sDeviceID, bMessage);
	}
	//设备上传覆冰数据报
	private static  void setIceThicknessDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[66];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x22&0xFF); //报文类型，导线覆冰是0x22

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(53&0xFF); // 等值覆冰厚度
		bMessage[47] = (byte)(53&0xFF);
		bMessage[48] = (byte)(25&0xFF);
		bMessage[49] = (byte)(63&0xFF);

		bMessage[50] = (byte)(53&0xFF); // 综合悬挂载荷
		bMessage[51] = (byte)(53&0xFF);
		bMessage[52] = (byte)(25&0xFF);
		bMessage[53] = (byte)(63&0xFF);

		bMessage[54] = (byte)(0&0xFF); // 不均衡张力差
		bMessage[55] = (byte)(0&0xFF);
		bMessage[56] = (byte)(0&0xFF);
		bMessage[57] = (byte)(0&0xFF);

		bMessage[58] = (byte)(53&0xFF); // 绝缘子串风偏角
		bMessage[59] = (byte)(53&0xFF);
		bMessage[60] = (byte)(25&0xFF);
		bMessage[61] = (byte)(63&0xFF);

		bMessage[62] = (byte)(53&0xFF); // 绝缘子串偏斜角
		bMessage[63] = (byte)(53&0xFF);
		bMessage[64] = (byte)(25&0xFF);
		bMessage[65] = (byte)(63&0xFF);
		sendCommand(sDeviceID, bMessage);
	}
	//设备上传导线温度数据报
	private static  void setlineTempDate(String sDeviceID) throws IOException{
		//监测数据通用部分
		byte[] bMessage = new byte[66];
		bMessage[21] = (byte)(0x01&0xFF);//帧类型
		bMessage[22] = (byte)(0x21&0xFF); //报文类型，导线温度是0x21

		Date date = new Date();
		long second = (long)(date.getTime()/1000);
		bMessage[40] = (byte)(second&0xFF); //采集时间
		bMessage[41] = (byte)((second >>8)&0xFF);
		bMessage[42] = (byte)((second >>16)&0xFF);
		bMessage[43] = (byte)((second >>24)&0xFF);

		bMessage[44] = (byte)(0&0xFF); //告警标识
		bMessage[45] = (byte)(0&0xFF);

		//监测数据不同部分
		bMessage[46] = (byte)(53&0xFF);// 线温1
		bMessage[47] = (byte)(53&0xFF);
		bMessage[48] = (byte)(25&0xFF);
		bMessage[49] = (byte)(63&0xFF);

		bMessage[50] = (byte)(53&0xFF);	// 线温2
		bMessage[51] = (byte)(53&0xFF);
		bMessage[52] = (byte)(25&0xFF);
		bMessage[53] = (byte)(63&0xFF);

		sendCommand(sDeviceID, bMessage);
	}
	//设备上传设备状态数据报
	private static  void setDeviceStateDate(String sDeviceID) throws IOException{

		byte[] bMessage = new byte[41];
		bMessage[21] = (byte)(0x07&0xFF);//帧类型
		bMessage[22] = (byte)(0xea&0xFF); //报文类型，设备状态是0xea

		bMessage[23] = (byte)(2&0xFF); //数据类型，2表示工作电压
		bMessage[24] = (byte)(4&0xFF);  //数据长度是4
		bMessage[25] = (byte)(11&0xFF);  //数据值
		bMessage[26] = (byte)(0&0xFF);
		bMessage[27] = (byte)(0&0xFF);
		bMessage[28] = (byte)(0&0xFF);

		bMessage[29] = (byte)(4&0xFF); //数据类型，4表示工作电流
		bMessage[30] = (byte)(4&0xFF);  //数据长度是4
		bMessage[31] = (byte)(53&0xFF);  //数据值
		bMessage[32] = (byte)(53&0xFF);
		bMessage[33] = (byte)(25&0xFF);
		bMessage[34] = (byte)(63&0xFF);

		bMessage[35] = (byte)(51&0xFF); //数据类型，51表示工作温度
		bMessage[36] = (byte)(4&0xFF);  //数据长度是4
		bMessage[37] = (byte)(53&0xFF);  //数据值
		bMessage[38] = (byte)(53&0xFF);
		bMessage[39] = (byte)(25&0xFF);
		bMessage[40] = (byte)(63&0xFF);

//		bMessage[41] = (byte)(3&0xFF); //数据类型，3表示工作电池剩余电量
//		bMessage[42] = (byte)(4&0xFF);  //数据长度是4
//		bMessage[43] = (byte)(0&0xFF);  //数据值
//		bMessage[44] = (byte)(0&0xFF);
//		bMessage[45] = (byte)(0&0xFF);
//		bMessage[46] = (byte)(0&0xFF);

//		bMessage[47] = (byte)(81&0xFF); //数据类型，81主机主板软件版本号
//		bMessage[48] = (byte)(4&0xFF);  //数据长度是4
//		bMessage[49] = (byte)(53&0xFF);  //数据值
//		bMessage[50] = (byte)(53&0xFF);
//		bMessage[51] = (byte)(25&0xFF);
//		bMessage[52] = (byte)(63&0xFF);

//		bMessage[53] = (byte)(82&0xFF); //数据类型，82主机主板硬件版本
//		bMessage[54] = (byte)(14&0xFF);  //数据长度是14
//		bMessage[55] = (byte)(52&0xFF);  //数据值
//		bMessage[56] = (byte)(52&0xFF);
//		bMessage[57] = (byte)(24&0xFF);
//		bMessage[58] = (byte)(62&0xFF);
//		bMessage[59] = (byte)(52&0xFF);  //数据值
//		bMessage[60] = (byte)(52&0xFF);
//		bMessage[61] = (byte)(24&0xFF);
//		bMessage[62] = (byte)(62&0xFF);
//		bMessage[63] = (byte)(52&0xFF);  //数据值
//		bMessage[64] = (byte)(52&0xFF);
//		bMessage[65] = (byte)(24&0xFF);
//		bMessage[66] = (byte)(62&0xFF);
//		bMessage[67] = (byte)(24&0xFF);
//		bMessage[68] = (byte)(62&0xFF);

		sendCommand(sDeviceID, bMessage);
	}


	//定时发送数据任务
	static class SentDataTask extends java.util.TimerTask{

		public void run() {
			Iterator itr = m_DeviceList.iterator();
			while (itr.hasNext()) {
				String sID = (String)itr.next();
				try {
					setMeteorologyDate(sID);
					Thread.sleep(100);
					setPoleTiltDate(sID);
					Thread.sleep(100);
					setAeolianVibrationDate(sID);
					Thread.sleep(100);
					setConductorSagDate(sID);
					Thread.sleep(100);
					setWindageYawDate(sID);
					Thread.sleep(100);
					setDeviceStateDate(sID);
					Thread.sleep(100);
					setIceThicknessDate(sID);
					Thread.sleep(100);
					setlineTempDate(sID);
					Thread.sleep(100);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
