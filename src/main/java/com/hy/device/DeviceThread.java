package com.hy.device;

import com.hy.bean.Header;
import com.hy.bean.NettyMessage;
import com.hy.device.resolver.DataResolver;
import com.hy.utils.BigEndian;
import com.hy.utils.ByteHelper;
import com.hy.utils.CommonFunctions;
import com.hy.utils.SocketCommFunc;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by cpazstido on 2016/6/1.
 */
public class DeviceThread extends Thread {
    public static Logger logger = Logger.getLogger(DeviceThread.class);
    //接收消息的消息头的长度和消息体的长度定义
    final int MSG_HEAD_BUF_LENGTH = 14;
    final int MSG_BODY_BUF_LENGTH = 4096;
    public Socket tcpSocket;
    //重连时间
    final int RECONNECT_TIME = 10000;
    public static BufferedOutputStream outputStream = null;
    private static BufferedInputStream inputStream = null;
    public String ip = null;
    public int port = 1;
    public String deviceID = null;


    public DeviceThread(String ip, int port, String deviceID) {
        this.ip = ip;
        this.port = port;
        this.deviceID = deviceID;
        logger.debug("============================="+deviceID+"=============================");
    }

    public void run() {
        int rsvSize = 0;
        byte[] msgHeadBuf = new byte[MSG_HEAD_BUF_LENGTH];
        while (true) {
            try {
                //1，判断socket是否建立，如果没有建立就重新建立socket
                if (tcpSocket == null) {
                    try {
                        tcpSocket = new Socket(ip, port);
                        inputStream = new BufferedInputStream(tcpSocket.getInputStream());
                        outputStream = new BufferedOutputStream(tcpSocket.getOutputStream());
                        logger.debug("Server connect=" + "已连接");
                    } catch (UnknownHostException e) {
                        try {
                            logger.info("Server connect=" + "未连接," + RECONNECT_TIME / 1000 + "秒后重连");
                            Thread.sleep(RECONNECT_TIME);
                        } catch (InterruptedException e1) {
                        }
                        continue;
                    } catch (IOException e) {
                        try {
                            logger.info("Server connect=" + "未连接," + RECONNECT_TIME / 1000 + "秒后重连");
                            Thread.sleep(RECONNECT_TIME);
                        } catch (InterruptedException e1) {
                        }
                        continue;
                    }

                }
                //2，把报文头取出来
                rsvSize = SocketCommFunc.receive(inputStream, msgHeadBuf, MSG_HEAD_BUF_LENGTH);
                if (rsvSize <= 0) {
                    logger.error("receive error, close socket iRsvSize = " + rsvSize);
                    try {
                        logger.info("Server connect=" + "未连接," + RECONNECT_TIME / 1000 + "秒后重连");
                        Thread.sleep(RECONNECT_TIME);
                        tcpSocket.close();
                        Thread.sleep(200);
                        tcpSocket = new Socket(ip, port);
                        inputStream = new BufferedInputStream(tcpSocket.getInputStream());
                        outputStream = new BufferedOutputStream(tcpSocket.getOutputStream());
                        logger.info("Server connect=" + "已连接");
                    } catch (UnknownHostException e) {
                    } catch (IOException e) {
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                //logger.debug("head rsvSize = " + rsvSize);
                byte[] bytesFlag = new byte[4];
                bytesFlag = ByteHelper.subBytes(msgHeadBuf, 0, 4);
                byte[] bytesVersion = new byte[1];
                bytesVersion = ByteHelper.subBytes(msgHeadBuf, 4, 1);
                byte[] bytesIndex = new byte[4];
                bytesIndex = ByteHelper.subBytes(msgHeadBuf, 5, 4);
                byte[] bytesType = new byte[1];
                bytesType = ByteHelper.subBytes(msgHeadBuf, 9, 1);
                byte[] bytesLength = new byte[4];
                bytesLength = ByteHelper.subBytes(msgHeadBuf, 10, 4);
                int bodyLength = BigEndian.getBigEndianInt(bytesLength);

                byte[] dataBytes = new byte[bodyLength + 14];
                System.arraycopy(bytesFlag, 0, dataBytes, 0, 4);
                System.arraycopy(bytesVersion, 0, dataBytes, 4, 1);
                System.arraycopy(bytesIndex, 0, dataBytes, 5, 4);
                System.arraycopy(bytesType, 0, dataBytes, 9, 1);
                System.arraycopy(bytesLength, 0, dataBytes, 10, 4);

                //3，循环接收报文内容，直到所有的消息内容接收完毕
                int iRealRcvSize = 0; //实际接收的数据内容长度
                byte[] msgBodyBuf = new byte[bodyLength];
                rsvSize = SocketCommFunc.receive(inputStream, msgBodyBuf, bodyLength);
                if (rsvSize < 0) {
                    logger.error("receive error, close socket iRsvSize = " + rsvSize);
                    try {
                        tcpSocket.close();
                        tcpSocket = null;
                    } catch (IOException e) {
                    }
                    continue;
                }
                //logger.debug("body iRsvSize = " + rsvSize);
                if (rsvSize != bodyLength) {
                    logger.error("指示的数据长度和实际接收的数据长度不一致");
                    continue;
                }
                System.arraycopy(msgBodyBuf, 0, dataBytes, 14, rsvSize);

                NettyMessage message = new NettyMessage();
                Header header = new Header();

                header.setFlag(bytesFlag);
                header.setVersion(bytesVersion[0]);
                header.setIndex(BigEndian.getBigEndianInt(bytesIndex));
                header.setTypes(bytesType[0]);
                header.setLen(BigEndian.getBigEndianInt(bytesLength));
                message.setHeader(header);
                message.setBody(msgBodyBuf);

                //logger.debug("类型：" + (int) message.getHeader().getTypes());
                DataResolver dataResolver;
                NettyMessage outMessage;
                logger.debug("收到数据：" + CommonFunctions.byteToHexStr(dataBytes, 14 + rsvSize));
                switch (message.getHeader().getTypes()) {
                    case 0:
                        logger.debug("收到登录挑战！");
                        dataResolver = new DataResolver();
                        outMessage = dataResolver.loginChallengeResolver(this, message);
                        logger.debug("发送登录挑战回应："+CommonFunctions.byteToHexStr(CommonFunctions.nettyMessageToBytes(outMessage), CommonFunctions.nettyMessageToBytes(outMessage).length));
                        outputStream.write(CommonFunctions.nettyMessageToBytes(outMessage));
                        outputStream.flush();
                        break;
                    case 1:
                        logger.debug("收到信息报！！");
                        dataResolver = new DataResolver();
                        String info = dataResolver.infoResolver(this, message);
                        logger.debug("info:"+info);
                        break;
                    case 2:
                        logger.debug("收到心跳包！！");
                        dataResolver = new DataResolver();
                        outMessage = dataResolver.heartBeatResolver(this, message);
                        logger.debug("发送心跳回应："+CommonFunctions.byteToHexStr(CommonFunctions.nettyMessageToBytes(outMessage), CommonFunctions.nettyMessageToBytes(outMessage).length));
                        outputStream.write(CommonFunctions.nettyMessageToBytes(outMessage));
                        outputStream.flush();
                        break;
                    case 3:
                        logger.debug("收到XML格式命令！！");
                        dataResolver = new DataResolver();
                        outMessage = dataResolver.xmlResolver(this, message);
                        logger.debug("发送XML格式命令查询回应："+CommonFunctions.byteToHexStr(CommonFunctions.nettyMessageToBytes(outMessage), CommonFunctions.nettyMessageToBytes(outMessage).length));
                        outputStream.write(CommonFunctions.nettyMessageToBytes(outMessage));
                        outputStream.flush();
                        break;
                    case 4:
                        logger.debug("收到数据部分为字符串的数据包！");
                        dataResolver = new DataResolver();
                        String data = dataResolver.textDataResolver(this, message);
                        logger.debug("data:"+data);
                        break;
                    default:
                }
            } catch (Exception e) {
                logger.debug(e);
                e.printStackTrace();
            }
        }
    }
}
