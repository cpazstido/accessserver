package com.hy.main;
import com.hy.server.FireServerDataAgent;
import com.hy.server.FireServerSigAgent;
import com.hy.server.StatusDataServerAgent;
import com.hy.server.WebServerAgent;
import com.hy.utils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * Created by cpazstido on 2016/5/23.
 */
public class MainServer {
    private static Logger logger = Logger.getLogger(MainServer.class);
    public static void main(String[] args) {
        try{
            //开启web信令通道线程
            new Thread("WebSig") {
                @Override
                public void run() {
                    try {
                        int port = Integer.parseInt(PropertyUtils.getValue("WebServerSigPort"));
                        WebServerAgent webServerAgent = new WebServerAgent("WebServerSigAgent", port);
                        webServerAgent.bind();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            Thread.sleep(500);

            //开启接收设备、传感器状态线程
            new Thread("StatusData") {
                @Override
                public void run() {
                    try {
                        int port = Integer.parseInt(PropertyUtils.getValue("StatusDataServerPort"));
                        StatusDataServerAgent statusDataServerAgent = new StatusDataServerAgent("StatusDataServerAgent", port);
                        statusDataServerAgent.bind();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            Thread.sleep(500);

            //开启fire信令通道线程
            new Thread("FireSig") {
                @Override
                public void run() {
                    try {
                        int port = Integer.parseInt(PropertyUtils.getValue("FireServerSigPort"));
                        FireServerSigAgent fireServerAgent = new FireServerSigAgent("FireServerSigAgent", port);
                        fireServerAgent.bind();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            //开启fire信令通道线程
            new Thread("FireData") {
                @Override
                public void run() {
                    try {
                        int port = Integer.parseInt(PropertyUtils.getValue("FireServerDataPort"));
                        FireServerDataAgent fireServerDataAgent = new FireServerDataAgent("FireServerDataAgent", port);
                        fireServerDataAgent.bind();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch (Exception e){
            logger.error(e);
        }
    }
}
