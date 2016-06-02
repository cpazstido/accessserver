package com.hy.handler;

import com.hy.bean.DeviceInfo;
import com.hy.bean.Header;
import com.hy.bean.MessageTypeReq;
import com.hy.bean.NettyMessage;
import com.hy.resolver.WebDataResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by cpazstido on 2016/5/20.
 */
public class WebServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;
    private static int cmdIndex = 1000;
    public static ConcurrentHashMap webClients = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger(WebServerHandler.class);
    public synchronized static int getCMDIndex(){
        return cmdIndex++;
    }

    public WebServerHandler(String url) {
        this.url = url;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws InterruptedException {
        try {
            final String uri = request.getUri();
            if (request.getMethod().equals(HttpMethod.POST)) {

                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                try{
                    List<InterfaceHttpData> postList = decoder.getBodyHttpDatas();
                    // 读取从客户端传过来的参数
                    for (InterfaceHttpData data : postList) {
                        String name = data.getName();
                        logger.info(data.toString());
                        String value = null;
                        if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                            MemoryAttribute attribute = (MemoryAttribute) data;
                            attribute.setCharset(CharsetUtil.UTF_8);
                            value = attribute.getValue();
                            logger.info("name:"+name+",value:"+value);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


            logger.debug(ctx.channel().remoteAddress()+" url:" + uri);
            if(!uri.contains("=")){
                logger.debug("error url!");
                return ;
            }
            String[] strs = uri.split("\\=");
            //logger.debug("url deviceid:"+strs[1]);
            int index = getCMDIndex();
            webClients.put(""+index, ctx);
            DeviceInfo deviceInfo = (DeviceInfo) FireServerHandler.fireClients.get(strs[1]);
            if(deviceInfo != null) {
                NettyMessage nettyMessage = new NettyMessage();
                String body = null;
                WebDataResolver webDataResolver = new WebDataResolver();
                body = webDataResolver.writeXmlForGetDeviceID("I want to get device ID");
                Header header = new Header();
                header.setLen(body.length());
                header.setFlag("HYVC".getBytes());
                header.setIndex(index);
                header.setVersion((byte) 1);
                header.setTypes(MessageTypeReq.XML_CMD.value());
                nettyMessage.setHeader(header);
                nettyMessage.setBody(body.getBytes());
                nettyMessage.setHeader(header);
                deviceInfo.getFsh().channelHandlerContext.writeAndFlush(nettyMessage);
            }else{
                logger.debug("nulllllllllllllllll");
            }
        }catch (Exception e){
            logger.error(e);
        }
        //sendListing(ctx);
        //Thread.sleep(6000);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendListing(final ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        StringBuilder buf = new StringBuilder();
        JSONObject json=new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("info","success");
        member1.put("loginname", "zhangfan");
        member1.put("password", "userpass");
        member1.put("email","10371443@qq.com");
        member1.put("sign_date", "2007-06-12");
        //buf.append("success_jsonpCallback(");
        buf.append(member1.toString());
        //buf.append(")");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer,buf.toString().getBytes().length);
        System.out.println(buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}