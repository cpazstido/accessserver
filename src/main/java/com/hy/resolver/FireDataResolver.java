package com.hy.resolver;

import com.hy.bean.*;
import com.hy.handler.FireServerHandler;
import com.hy.handler.HeartBeatTask;
import com.hy.handler.WebServerHandler;
import com.hy.utils.PropertyUtils;
import com.hy.utils.RedisUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.IntegerPatternConverter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by cpazstido on 2016/5/24.
 */
public class FireDataResolver {
    private static Logger logger = Logger.getLogger(FireDataResolver.class);

    /**
     * 登录数据分析
     * 分析数据后，若登录成功，则将loginSuccess设为true,下一步，设备应该发送设备信息给服务器，服务器保存相关信息
     * 若失败，将设备加入黑名单，30分钟内不能登录
     *
     * @param fsh
     * @param message
     */
    public void LoginDataResolver(FireServerHandler fsh, NettyMessage message) {
        String deviceID = "", randomCode = "", ipcNum = "", softwareVersion = "", data = "";
        DeviceInfo deviceInfo = new DeviceInfo();
        try {
            String body = (String) message.getBody();
            //logger.debug("开始解析:"+ body);
            Document doc = null;
            doc = DocumentHelper.parseText(body); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String EventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            if (EventType != null && EventType.equals("LoginChallenge")) {
                Element eDeviceID = rootElt.element("DeviceId");
                deviceID = eDeviceID.getText();
                deviceInfo.setDeviceId(deviceID);
                Element eIpcNum = rootElt.element("IPCNum");
                ipcNum = eIpcNum.getText();
                deviceInfo.setIpcNum(Integer.parseInt(ipcNum));
                Element eSoftwareVersion = rootElt.element("SoftwareVersion");
                softwareVersion = eSoftwareVersion.getText();
                deviceInfo.setSoftwareVersion(Float.parseFloat(softwareVersion));
                Element eData = rootElt.element("Data");
                data = eData.getText();
                deviceInfo.setData(data.getBytes());
                deviceInfo.setDeviceId(deviceID);
                deviceInfo.setFsh(fsh);
                logger.debug("deviceID:" + deviceID);
                Element eKey = rootElt.element("RandomCode");
                randomCode = eKey.getText();
                logger.debug("randomCode:" + randomCode);
                //logger.debug("解析:"+ body+" 完毕！");
            } else {
                logger.error("xml不符合规范！");
            }
        } catch (Exception e) {
            logger.error(e);
            fsh.loginChallengeSchedule.cancel(true);
            return;
        }

        //对key进行相关验证，验证成功则将设备加入fireClient,失败则踢掉
        if (validateKey(fsh.randomCode, randomCode)) {
            //成功
            fsh.loginSuccess = true;
            fsh.channelHandlerContext.writeAndFlush(buildLoginInfoResp("login success!"));
            fsh.deviceId = deviceID;
            fsh.fireClients.put(deviceID, deviceInfo);
            fsh.loginChallengeSchedule.cancel(true);
            logger.debug(fsh.channelHandlerContext.channel().remoteAddress() + " 登录成功！");
        } else {
            //失败
            fsh.loginSuccess = false;
            fsh.channelHandlerContext.writeAndFlush(buildLoginInfoResp("login error!")).addListener(ChannelFutureListener.CLOSE);
            Jedis jedis = RedisUtil.getJedis();
            jedis.set(fsh.ip, fsh.ip);
            jedis.expire(fsh.ip, Integer.parseInt(PropertyUtils.getValue("BlacklistTimeout")));
            fsh.loginChallengeSchedule.cancel(true);
        }
        fsh.loginSuccess = true;
    }

    public boolean validateKey(String randomCode, String key) {
        return true;
    }

    public NettyMessage buildInfoResp(String info) {
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

    public NettyMessage buildLoginInfoResp(String info) {
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
        root.addAttribute("EventType", "LoginChallenge");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

    public String writeXmlForInfoResp(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType", "Info");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

    public void handleHeartBeatResp(FireServerHandler fireServerHandler, ChannelHandlerContext ctx) {
        //处理心跳回应
        synchronized (this) {
            fireServerHandler.LoseHeartbeatTimes = 0;
        }
    }

    public void handleXMLResp(FireServerHandler fireServerHandler, ChannelHandlerContext ctx, NettyMessage message, FullHttpResponse response) {
        String body = (String) message.getBody();
        //logger.debug("收到xml数据:"+body);
        ChannelHandlerContext ctxx = (ChannelHandlerContext) WebServerHandler.webClients.get("" + message.getHeader().getIndex());
        try {
            if (ctxx != null) {
                Document doc = null;
                doc = DocumentHelper.parseText(body); // 将字符串转为XML
                Element rootElt = doc.getRootElement(); // 获取根节点
                String sEventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
                if (sEventType != null && sEventType.compareTo("GetDeviceID") == 0) {
                    //获取设备id
                } else if (sEventType != null && sEventType.compareTo("GetPicture") == 0) {
                    //获取图片
                    Element eInfo = rootElt.element("Info");
                    String info = eInfo.getText();
                    sendGetPicture(response, ctxx, message.getHeader().getIndex(), info);
                }else if (sEventType != null && sEventType.compareTo("SetTime") == 0){
                    //网络校时
                    Element eInfo = rootElt.element("Info");
                    String info = eInfo.getText();
                    sendSetTime(response, ctxx, message.getHeader().getIndex(), info);
                }
            } else {
                logger.debug("nullllllllllllllllllllllllllllll");
            }
        } catch (Exception e) {
            logger.debug(e);
        }

    }

    public void handleTXTResp(FireServerHandler fireServerHandler, ChannelHandlerContext ctx, NettyMessage message, FullHttpResponse response) {
        //文本数据
        //logger.debug("=================="+message.getBody());
        ChannelHandlerContext ctxx = (ChannelHandlerContext) WebServerHandler.webClients.get("" + message.getHeader().getIndex());
        if (ctxx != null) {
            //sendGetDeviceID(response, ctxx, message.getHeader().getIndex(), ((String) message.getBody()).getBytes());
            sendInfo(response, ctxx, message.getHeader().getIndex(), ((String) message.getBody()).getBytes());
        } else {
            logger.debug("nullllllllllllllllllllllllllllll");
        }
    }

    public void handleLogin(FireServerHandler fireServerHandler, ChannelHandlerContext ctx, NettyMessage message) {
        //处理登录信息
        logger.debug("收到(" + ctx.channel().remoteAddress() + ")登录数据报：" + (String) message.getBody());
        if (fireServerHandler.loginSuccess) {
            //重复登录，不处理，直接扔掉
            logger.debug(ctx.channel().remoteAddress() + " 重复登录！");
            return;
        }
        LoginDataResolver(fireServerHandler, message);
        if (fireServerHandler.loginSuccess) {
            //登录成功，发心跳
            fireServerHandler.heartBeatSchedule = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(fireServerHandler), 0, Integer.parseInt(PropertyUtils.getValue("HeartbeatInterval")), TimeUnit.MILLISECONDS);
            return;
        } else {
            ctx.close();
            return;
        }
    }

    public void handleData(FireServerHandler fireServerHandler, ChannelHandlerContext ctx, NettyMessage message) {
        //分类型处理回复数据
        //设置回应头
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        switch (message.getHeader().getTypes()) {
            case 0:
                logger.debug("收到type:LOGIN_Resp" + " 登录回应");
                handleLogin(fireServerHandler, ctx, message);
                break;
            case 1:
                logger.debug("收到type:HEARTBEAT_Resp" + " 心跳回应");
                handleHeartBeatResp(fireServerHandler, ctx);
                break;
            case 2:
                logger.debug("收到type:CMMD_RESP_RUNNING" + " 命令收到，正在执行，结果将通过数据通道返回");
                break;
            case 3:
                logger.debug("收到type:CMMD_RESP_XML_RESULT" + " 命令收到，且执行完毕，数据为执行结果(xml格式)");
                handleXMLResp(fireServerHandler, ctx, message, response);
                break;
            case 4:
                logger.debug("收到type:CMMD_RESP_TXT_RESULT" + " 命令收到，且执行完毕，数据为执行结果(字符串格式)");
                handleTXTResp(fireServerHandler, ctx, message, response);
                break;
            case 5:
                logger.debug("收到type:CMMD_RESP_ERROR" + " 命令执行出错（数据为出错信息）");
                //handleXMLResp(fireServerHandler, ctx, message, response);
                break;
            default:
                logger.debug("收到未知回应" + "");
        }
    }

    public void sendGetPicture(FullHttpResponse response, ChannelHandlerContext ctx, int index, String info) {
        try {
            StringBuilder buf = new StringBuilder();
            JSONObject member1 = new JSONObject();
            member1.put("info", info);
            buf.append(member1.toString());
            ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
            response.content().writeBytes(buffer, buf.toString().getBytes().length);
            logger.debug("返回给web的数据:" + buf.toString());
            buffer.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            logger.debug("remove web!" + index);
            WebServerHandler.webClients.remove("" + index);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void sendSetTime(FullHttpResponse response, ChannelHandlerContext ctx, int index, String info) {
        try {
            StringBuilder buf = new StringBuilder();
            JSONObject member1 = new JSONObject();
            member1.put("info", info);
            buf.append(member1.toString());
            ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
            response.content().writeBytes(buffer, buf.toString().getBytes().length);
            logger.debug("返回给web的数据:" + buf.toString());
            buffer.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            logger.debug("remove web!" + index);
            WebServerHandler.webClients.remove("" + index);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void sendGetDeviceID(FullHttpResponse response, final ChannelHandlerContext ctx, final int index, final byte[] deviceId) {
        StringBuilder buf = new StringBuilder();
        JSONObject json = new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("info", new String(deviceId));
        buf.append(member1.toString());
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer, buf.toString().getBytes().length);
        logger.debug("返回给web的数据:" + buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        WebServerHandler.webClients.remove("" + index);
    }

    public void sendInfo(FullHttpResponse response, final ChannelHandlerContext ctx, final int index, final byte[] info) {
        StringBuilder buf = new StringBuilder();
        JSONObject json = new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("info", new String(info));
        buf.append(member1.toString());
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer, buf.toString().getBytes().length);
        logger.debug("返回给web的数据:" + buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        WebServerHandler.webClients.remove("" + index);
    }
}
