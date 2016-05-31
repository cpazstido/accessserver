package com.hy.resolver;

import com.hy.bean.Header;
import com.hy.bean.MessageTypeReq;
import com.hy.bean.NettyMessage;
import com.hy.handler.FireServerHandler;
import com.hy.utils.PropertyUtils;
import com.hy.utils.RedisUtil;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;

/**
 * Created by cpazstido on 2016/5/24.
 */
public class FireDataResolver {
    private  static Logger logger = Logger.getLogger(FireDataResolver.class);
    /**
     * 登录数据分析
     * 分析数据后，若登录成功，则将loginSuccess设为true,下一步，设备应该发送设备信息给服务器，服务器保存相关信息
     * 若失败，将设备加入黑名单，30分钟内不能登录
     * @param fsh
     * @param message
     */
    public void LoginDataResolver(FireServerHandler fsh, NettyMessage message){
        String deviceID="",randomCode="";
        try {
            String body = (String) message.getBody();
            //logger.debug("开始解析:"+ body);
            Document doc = null;
            doc = DocumentHelper.parseText(body); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String EventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            if(EventType != null &&EventType.equals("LoginChallenge")){
                Element eDeviceID=rootElt.element("DeviceId");
                deviceID = eDeviceID.getText();
                logger.debug("deviceID:"+deviceID);
                Element eKey=rootElt.element("RandomCode");
                randomCode=eKey.getText();
                logger.debug("randomCode:"+randomCode);
                //logger.debug("解析:"+ body+" 完毕！");
            }else{
                logger.error("xml不符合规范！");
            }
        } catch (Exception e) {
            logger.error(e);
            fsh.loginChallengeSchedule.cancel(true);
            return ;
        }

        //对key进行相关验证，验证成功则将设备加入fireClient,失败则踢掉
        if(validateKey(fsh.randomCode, randomCode)){
            //成功
            fsh.loginSuccess = true;
            fsh.channelHandlerContext.writeAndFlush(buildLoginInfoResp("login success!"));
            fsh.deviceId = deviceID;
            fsh.fireClients.put(deviceID,fsh.channelHandlerContext);
            fsh.loginChallengeSchedule.cancel(true);
            logger.debug(fsh.channelHandlerContext.channel().remoteAddress()+" 登录成功！");
        }else{
            //失败
            fsh.loginSuccess = false;
            fsh.channelHandlerContext.writeAndFlush(buildLoginInfoResp("login error!")).addListener(ChannelFutureListener.CLOSE);
            Jedis jedis = RedisUtil.getJedis();
            jedis.set(fsh.ip,fsh.ip);
            jedis.expire(fsh.ip,Integer.parseInt(PropertyUtils.getValue("BlacklistTimeout")));
            fsh.loginChallengeSchedule.cancel(true);
        }
        fsh.loginSuccess = true;
    }

    public boolean validateKey(String randomCode, String key){
        return true;
    }

    public NettyMessage buildInfoResp(String info){
        NettyMessage message = new NettyMessage();
        String body = writeXmlForInfoResp(info);
        Header header = new Header();
        header.setLen(body.length());
        header.setFlag("HYVC".getBytes());
        header.setIndex(0);
        header.setVersion((byte) 1);
        header.setTypes(MessageTypeReq.INFO.value());
        message.setHeader(header);
        message.setBody(body.getBytes());
        return message;
    }

    public NettyMessage buildLoginInfoResp(String info){
        NettyMessage message = new NettyMessage();
        String body = writeXmlForLoginInfoResp(info);
        Header header = new Header();
        header.setLen(body.length());
        header.setFlag("HYVC".getBytes());
        header.setIndex(0);
        header.setVersion((byte) 1);
        header.setTypes(MessageTypeReq.LOGIN_INFO.value());
        message.setHeader(header);
        message.setBody(body.getBytes());
        return message;
    }

    public String writeXmlForLoginInfoResp(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType","LoginChallenge");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

    public String writeXmlForInfoResp(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType","Info");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

}
