package com.hy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by cpazstido on 2016/5/20.
 */
public class WebServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;
    private static int cmdIndex = 0;
    public static ConcurrentHashMap webClients = new ConcurrentHashMap();
    private static Logger logger=Logger.getLogger("WebServerHandler");
    public synchronized static int getCMDIndex(){
        return cmdIndex++;
    }

    public WebServerHandler(String url) {
        this.url = url;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            logger.debug(ctx.channel().remoteAddress());
            final String uri = request.getUri();
            logger.debug(" url:" + uri);
            if(!uri.contains("=")){
                logger.debug("error url!");
                return ;
            }
            String[] strs = uri.split("\\=");
            logger.debug("url deviceid:"+strs[1]);
            int index = getCMDIndex();
            webClients.put(""+index, ctx);
            ChannelHandlerContext chc = (ChannelHandlerContext) FireServerHandler.fireClients.get(strs[1]);
            chc.writeAndFlush(""+index);
        }catch (Exception e){
            logger.error(e);
        }
        //sendListing(ctx);
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
        StringBuilder buf = new StringBuilder();
        JSONObject json=new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("loginname", "zhangfan");
        member1.put("password", "userpass");
        member1.put("email","10371443@qq.com");
        member1.put("sign_date", "2007-06-12");
        buf.append("success_jsonpCallback(");
        buf.append(member1.toString());
        buf.append(")");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer,buf.toString().getBytes().length);
        System.out.println(buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}