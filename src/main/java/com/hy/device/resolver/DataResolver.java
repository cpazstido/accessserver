package com.hy.device.resolver;

import com.hy.bean.Header;
import com.hy.bean.MessageTypeResp;
import com.hy.bean.NettyMessage;
import com.hy.device.DeviceThread;
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

}
