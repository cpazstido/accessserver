package com.hy.device.resolver;

import com.hy.bean.Header;
import com.hy.bean.MessageTypeResp;
import com.hy.bean.NettyMessage;
import com.hy.device.DeviceThread;
import com.hy.device.SendFileThread;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Created by cpazstido on 2016/6/1.
 */
public class DataResolver {
    public static Logger logger = Logger.getLogger(DataResolver.class);


    public NettyMessage loginChallengeResolver(DeviceThread deviceThread, NettyMessage message){
        String body = new String((byte[]) message.getBody());
        //logger.debug("body:"+ body);
        String randomCode = null;
        try {
            //logger.debug("开始解析:"+ body);
            Document doc = null;
            doc = DocumentHelper.parseText(body); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String EventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            if(EventType != null &&EventType.equals("LoginChallenge")){
                Element eRandomCode=rootElt.element("RandomCode");
                randomCode = eRandomCode.getText();
                //logger.debug("randomCode:"+randomCode);
                //logger.debug("解析:"+ body+" 完毕！");
            }else{
                logger.error("xml不符合规范！");
            }
        } catch (Exception e) {
            logger.error(e);
        }

        //回复消息
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType","LoginChallenge");
        Element eRandomCode = root.addElement("RandomCode");
        eRandomCode.setText(randomCode);
        Element eDeviceId = root.addElement("DeviceId");
        eDeviceId.setText(deviceThread.deviceID);
        Element eIPCNum = root.addElement("IPCNum");
        eIPCNum.setText("2");
        Element eSoftwareVersion = root.addElement("SoftwareVersion");
        eSoftwareVersion.setText("4.0");
        Element eData = root.addElement("Data");
        eData.setText("this is a test!");
        String oBody = document.asXML();
        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setVersion(message.getHeader().getVersion());
        header.setFlag(message.getHeader().getFlag());
        header.setIndex(message.getHeader().getIndex());
        header.setTypes(MessageTypeResp.LOGIN_Resp.value());
        header.setLen(oBody.length());
        nettyMessage.setHeader(header);
        nettyMessage.setBody(oBody.getBytes());
        return nettyMessage;
    }

    public NettyMessage heartBeatResolver(DeviceThread deviceThread, NettyMessage message){
        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setVersion(message.getHeader().getVersion());
        header.setFlag(message.getHeader().getFlag());
        header.setIndex(message.getHeader().getIndex());
        header.setTypes(MessageTypeResp.HEARTBEAT_Resp.value());
        header.setLen(0);
        nettyMessage.setHeader(header);
        return nettyMessage;
    }

    public String infoResolver(DeviceThread deviceThread, NettyMessage nettyMessage){
        String body = new String((byte[]) nettyMessage.getBody());
        String info = null;
        try {
            //logger.debug("开始解析:"+ body);
            Document doc = null;
            doc = DocumentHelper.parseText(body); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String EventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            if(EventType != null &&EventType.equals("LoginChallenge")){
                Element eInfo=rootElt.element("Info");
                info = eInfo.getText();
                //logger.debug("randomCode:"+randomCode);
                //logger.debug("解析:"+ body+" 完毕！");
            }else{
                logger.error("xml不符合规范！");
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return info;
    }

    public String textDataResolver(DeviceThread deviceThread, NettyMessage nettyMessage){
        String body = new String((byte[]) nettyMessage.getBody());
        return body;
    }

    public NettyMessage xmlResolver(DeviceThread deviceThread, NettyMessage message){
        String body = new String((byte[]) message.getBody());
        try {
            //logger.debug("开始解析:"+ body);
            Document doc = null;
            doc = DocumentHelper.parseText(body); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String sEventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            if(sEventType !=null && sEventType.compareTo("GetDeviceID") ==0){
                return getDeviceID(deviceThread, message);
            }else if(sEventType !=null && sEventType.compareTo("GetPicture") ==0){
                return getPicture(deviceThread,message);
            }
        }catch (Exception e){
            logger.debug(e);
        }
        logger.debug("nullllllllllllllllllllllllll");
        return null;
    }

    public NettyMessage getDeviceID(DeviceThread deviceThread, NettyMessage message){
        NettyMessage nettyMessage = new NettyMessage();
        String body = deviceThread.deviceID;
        Header header = new Header();
        header.setVersion(message.getHeader().getVersion());
        header.setFlag(message.getHeader().getFlag());
        header.setIndex(message.getHeader().getIndex());
        header.setTypes(MessageTypeResp.CMMD_RESP_TXT_RESULT.value());
        header.setLen(body.length());
        nettyMessage.setHeader(header);
        nettyMessage.setBody(body.getBytes());
        return nettyMessage;
    }

    public NettyMessage getPicture(DeviceThread deviceThread, NettyMessage message){
        NettyMessage nettyMessage = new NettyMessage();
        try {
            String body = writeXmlForGetPicture("success!picture will be send by data channel!");
            Header header = new Header();
            header.setVersion(message.getHeader().getVersion());
            header.setFlag(message.getHeader().getFlag());
            header.setIndex(message.getHeader().getIndex());
            header.setTypes(MessageTypeResp.CMMD_RESP_XML_RESULT.value());
            header.setLen(body.length());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());

            logger.debug("发送文件！");
            SendFileThread sendFileThread = new SendFileThread("172.16.16.112",8080);
            sendFileThread.start();
        }catch (Exception e){
            logger.error(e);
        }
        return nettyMessage;
    }

    public String writeXmlForGetPicture(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType", "GetPicture");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

}
