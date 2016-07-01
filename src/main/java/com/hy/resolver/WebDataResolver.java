package com.hy.resolver;

import com.hy.bean.*;
import com.hy.handler.FireServerHandler;
import com.hy.handler.WebServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.V;

/**
 * Created by cpazstido on 2016/6/1.
 */
public class WebDataResolver {
    private static Logger logger = Logger.getLogger(WebDataResolver.class);

    public String writeXmlForGetDeviceID(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType", "GetDeviceID");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

    public String writeXmlForGetPicture(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType", "GetPicture1");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }

    public String writeXmlForSetTime(int year, int month, int day, int hour, int minute, int second) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType", "SetTime");
        Element eYear = root.addElement("Year");
        Element eMonth = root.addElement("Month");
        Element eDay = root.addElement("Day");
        Element eHour = root.addElement("Hour");
        Element eMinute = root.addElement("Minute");
        Element eSecond = root.addElement("Second");
        eYear.setText(year+"");
        eMonth.setText(month+"");
        eDay.setText(day+"");
        eHour.setText(hour+"");
        eMinute.setText(minute+"");
        eSecond.setText(second+"");

        return document.asXML();
    }

    public void handleData(WebServerHandler webServerHandler, ChannelHandlerContext ctx, FullHttpRequest request) {
        String action = request.getUri().substring(1, request.getUri().length());
        logger.debug("action:" + action);
        if (action.compareTo("getDeviceID") == 0) {
            getDeviceID(webServerHandler,ctx);
        } else if (action.compareTo("getPicture1") == 0) {
            getPicture(webServerHandler,ctx);
        } else if (action.compareTo("setTime") == 0){
            setTime(webServerHandler,ctx);
        } else if (action.compareTo("PTZ") == 0){
            PTZ(webServerHandler,ctx);
        } else if (action.compareTo("ChangePTZMode") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("StartRealPlay") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("StopRealPlay") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("VideoEncoderConfiguration") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("SetPreset") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("GotoPreset") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("ClearPreset") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("SavePatrol") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("StartPatrol") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("StopPatrol") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("DeletePatrol") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("TaskVideo") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("TaskPic") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("VisualCameraPowerOn") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("VisualCameraPowerOff") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("ResetBoard") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("MobileFlow") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("GetPicture") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("SetDetectTime") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("GetResource") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("GetStat") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("SetMediaSrvAddr") ==0){
            ATOD_XML(webServerHandler, ctx);
        } else if (action.compareTo("DeviceOnlineList") ==0){//在线设备列表
            getOnlineDeviceList(webServerHandler, ctx);
        }
        else {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
        }
    }

    public void sendError(ChannelHandlerContext ctx,
                          HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public void getDeviceID(WebServerHandler webServerHandler, ChannelHandlerContext ctx) {
        int index = WebServerHandler.getCMDIndex();
        webServerHandler.webClients.put("" + index, ctx);
        DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(webServerHandler.postParam.get("deviceid"));
        if (deviceInfo != null) {
            NettyMessage nettyMessage = new NettyMessage();
            String body = null;
            body = writeXmlForGetDeviceID("I want to get device ID");
            Header header = new Header();
            header.setLen(body.length());
            header.setFlag(ConstantValue.FLAGS.getBytes());
            header.setIndex(index);
            header.setVersion(ConstantValue.VERSION);
            header.setTypes(MessageTypeReq.XML_CMD.value());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());
            nettyMessage.setHeader(header);
            deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
        } else {
            logger.debug("nulllllllllllllllll");
        }
    }

    public void getPicture(WebServerHandler webServerHandler, ChannelHandlerContext ctx) {
        int index = WebServerHandler.getCMDIndex();
        webServerHandler.webClients.put("" + index, ctx);
        DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(webServerHandler.postParam.get("deviceid"));
        if (deviceInfo != null) {
            NettyMessage nettyMessage = new NettyMessage();
            String body = null;
            body = writeXmlForGetPicture("I want to get a picture");
            Header header = new Header();
            header.setLen(body.length());
            header.setFlag(ConstantValue.FLAGS.getBytes());
            header.setIndex(index);
            header.setVersion(ConstantValue.VERSION);
            header.setTypes(MessageTypeReq.XML_CMD.value());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());
            nettyMessage.setHeader(header);
            deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
        } else {
            logger.debug("nulllllllllllllllll");
        }
    }

    public void getOnlineDeviceList(WebServerHandler webServerHandler, ChannelHandlerContext ctx){
        String type = (String) webServerHandler.postParam.get("type");
//        Document document = DocumentHelper.createDocument();
//        Element root = document.addElement("eMonitor_XML");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        if(type == null || type.compareTo("")==0){
            logger.error("type is null!");
            return ;
        }
        if(type.compareTo("fire") ==0 ){
            //获取山火列表
            ConcurrentHashMap<String,DeviceInfo> map=FireServerHandler.fireClients;
            List<Map> list = new ArrayList<Map>();
            JSONArray jsonArray = new JSONArray();
            for(Map.Entry<String,DeviceInfo> e: map.entrySet() ){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("DeviceID",e.getValue().getDeviceId());
                jsonObject.put("Type","fire");
                jsonObject.put("ChannelNum",e.getValue().getIpcNum()+"");
                jsonArray.put(jsonObject);
            }
            ByteBuf buffer = Unpooled.copiedBuffer(jsonArray.toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buffer, jsonArray.toString().getBytes().length);
            logger.debug("返回给web的数据:" + jsonArray.toString());
            buffer.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        else if(type.compareTo("break") ==0 ){}
        else if(type.compareTo("video") ==0 ){}
        else{}
    }

    public void setTime(WebServerHandler webServerHandler, ChannelHandlerContext ctx) {
        int index = WebServerHandler.getCMDIndex();
        webServerHandler.webClients.put("" + index, ctx);
        DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(webServerHandler.postParam.get("deviceid"));
        if (deviceInfo != null) {
            NettyMessage nettyMessage = new NettyMessage();
            String body = null;
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);//获取年份
            int month=cal.get(Calendar.MONTH)+1;//获取月份
            int day=cal.get(Calendar.DATE);//获取日
            int hour=cal.get(Calendar.HOUR_OF_DAY);//小时
            int minute=cal.get(Calendar.MINUTE);//分
            int second=cal.get(Calendar.SECOND);//秒
            body = writeXmlForSetTime(year, month, day, hour, minute, second);
            Header header = new Header();
            header.setLen(body.length());
            header.setFlag(ConstantValue.FLAGS.getBytes());
            header.setIndex(index);
            header.setVersion(ConstantValue.VERSION);
            header.setTypes(MessageTypeReq.XML_CMD.value());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());
            nettyMessage.setHeader(header);
            deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
        } else {
            logger.debug("nulllllllllllllllll");
        }
    }

    public void PTZ(WebServerHandler webServerHandler, ChannelHandlerContext ctx) {
        int index = WebServerHandler.getCMDIndex();
        webServerHandler.webClients.put("" + index, ctx);
        DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(webServerHandler.postParam.get("deviceid"));
        if (deviceInfo != null) {
            NettyMessage nettyMessage = new NettyMessage();
            String body = null;
            body = (String) webServerHandler.postParam.get("xml");
            logger.debug(body);
            Header header = new Header();
            header.setLen(body.length());
            header.setFlag(ConstantValue.FLAGS.getBytes());
            header.setIndex(index);
            header.setVersion(ConstantValue.VERSION);
            header.setTypes(MessageTypeReq.XML_CMD.value());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());
            nettyMessage.setHeader(header);
            deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
        } else {
            logger.debug("nulllllllllllllllll");
        }
    }

    public void ATOD_XML(WebServerHandler webServerHandler, ChannelHandlerContext ctx) {
        int index = WebServerHandler.getCMDIndex();
        webServerHandler.webClients.put("" + index, ctx);
        DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(webServerHandler.postParam.get("deviceid"));
        if (deviceInfo != null) {
            NettyMessage nettyMessage = new NettyMessage();
            String body = null;
            body = (String) webServerHandler.postParam.get("xml");
            logger.debug(body);
            Header header = new Header();
            header.setLen(body.length());
            header.setFlag(ConstantValue.FLAGS.getBytes());
            header.setIndex(index);
            header.setVersion(ConstantValue.VERSION);
            header.setTypes(MessageTypeReq.XML_CMD.value());
            nettyMessage.setHeader(header);
            nettyMessage.setBody(body.getBytes());
            nettyMessage.setHeader(header);
            deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
        } else {
            logger.debug(webServerHandler.postParam.get("deviceid")+" is not online!");
        }
    }
}
