package com.hy.resolver;

import com.hy.bean.*;
import com.hy.handler.FireServerHandler;
import com.hy.handler.WebServerHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Calendar;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
        root.addAttribute("EventType", "GetPicture");
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
        } else if (action.compareTo("getPicture") == 0) {
            getPicture(webServerHandler,ctx);
        } else if(action.compareTo("setTime") == 0){
            setTime(webServerHandler,ctx);
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
}
