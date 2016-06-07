package com.hy.device;

import org.apache.log4j.Logger;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created by cpazstido on 2016/6/3.
 */
public class SendFileThread extends Thread {
    private static Logger logger = Logger.getLogger(SendFileThread.class);
    private String ip = null;
    private int port = 0;
    private String filePath = null;
    private Socket tcpSocket = null;
    private BufferedInputStream bufferedInputStream = null;
    private BufferedOutputStream bufferedOutputStream = null;
    final int RECONNECT_TIME = 10; //重连时间

    public SendFileThread(){}

    public SendFileThread(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        int connnectTimes = 0;
        while (true) {
            try {
                if (connnectTimes > 2) {
                    logger.debug("连接次数多余三次！");
                    break;
                }
                logger.debug("第"+(connnectTimes+1)+"次连接服务器！");
                try {
                    if (tcpSocket == null) {
                        tcpSocket = new Socket(ip, port);
                        bufferedOutputStream = new BufferedOutputStream(tcpSocket.getOutputStream());
                        logger.debug("Server socket 已连接！");
                    }
                } catch (UnknownHostException e) {
                    logger.debug("Server Socket 连接失败！ "+RECONNECT_TIME +"s后重连！");
                    TimeUnit.SECONDS.sleep(RECONNECT_TIME);
                    connnectTimes++;
                    continue;
                } catch (IOException e){
                    logger.debug("Server Socket 连接失败！ "+RECONNECT_TIME +"s后重连！");
                    TimeUnit.SECONDS.sleep(RECONNECT_TIME);
                    connnectTimes++;
                    continue;
                }

                sendFile(bufferedOutputStream,"d:\\log.txt");
                logger.debug("end!");

                break;
            } catch (Exception e) {
                logger.error(e);
            }
        }
        if(tcpSocket != null){
            try {
                tcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.debug("ssssssss");
    }

    public void sendFile(BufferedOutputStream out,String filePath){
        try {
            File file = new File(filePath);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            out.write(filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length()).getBytes());
            out.flush();
            byte[] bytes = new byte[1024];
            int length = 0;
            while((length = bufferedInputStream.read(bytes, 0, bytes.length)) > 0){
                out.write(bytes, 0, length);
                out.flush();
            }
        }catch (Exception e){
            logger.error(e);
        }
    }
}
