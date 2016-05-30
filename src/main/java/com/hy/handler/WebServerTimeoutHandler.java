package com.hy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by cpazstido on 2016/5/27.
 */
public class WebServerTimeoutHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(WebServerTimeoutHandler.class);
    private final long timeoutMillis;
    private volatile ScheduledFuture<?> timeout;
    private volatile long lastReadTime;
    private volatile int state;
    private boolean closed;

    public WebServerTimeoutHandler(int timeoutSeconds) {
        this((long)timeoutSeconds, TimeUnit.SECONDS);
    }

    public WebServerTimeoutHandler(long timeout, TimeUnit unit) {
        if(unit == null) {
            throw new NullPointerException("unit");
        } else {
            if(timeout <= 0L) {
                this.timeoutMillis = 0L;
            } else {
                this.timeoutMillis = Math.max(unit.toMillis(timeout), 1L);
            }

        }
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        if(ctx.channel().isActive() && ctx.channel().isRegistered()) {
            //System.out.println("handlerAdded()");
            //this.initialize(ctx);
        }

    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("handlerRemoved()");
        this.destroy();
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("channelRegistered()");
        if(ctx.channel().isActive()) {
            //this.initialize(ctx);
        }

        super.channelRegistered(ctx);
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive() "+ctx.channel().remoteAddress());
        this.initialize(ctx);
        super.channelActive(ctx);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("channelInactive()");
        this.destroy();
        super.channelInactive(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DefaultFullHttpRequest defaultFullHttpRequest = (DefaultFullHttpRequest)msg;
        String url = defaultFullHttpRequest.getUri();
        logger.debug(ctx.channel().remoteAddress()+"url:======="+defaultFullHttpRequest.getUri());
        logger.debug("channelRead()========"+msg);
        this.lastReadTime = System.currentTimeMillis();
//        if(url.contains("test")){
//            ctx.fireChannelRead(msg);
//        }else{
//            ctx.close();
//        }
        ctx.fireChannelRead(msg);
    }

    private void initialize(ChannelHandlerContext ctx) {
        switch(this.state) {
            case 1:
            case 2:
                return;
            default:
                this.state = 1;
                this.lastReadTime = System.currentTimeMillis();
                //System.out.println(ctx.channel().remoteAddress()+" initialize() lastReadTime:"+lastReadTime);
                if(this.timeoutMillis > 0L) {
                    this.timeout = ctx.executor().schedule(new WebServerTimeoutHandler.ReadTimeoutTask(ctx), this.timeoutMillis, TimeUnit.MILLISECONDS);
                }
        }
    }

    private void destroy() {
        //System.out.println("destroy()");
        this.state = 2;
        if(this.timeout != null) {
            this.timeout.cancel(false);
            this.timeout = null;
        }

    }

    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("readTimedOut()");
        if(!this.closed) {
            logger.debug("timeout:"+ctx.channel().remoteAddress());
            sendListing(ctx);
            ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
            ctx.close();
            this.closed = true;
        }

    }

    private static void sendListing(final ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        //response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        JSONObject json=new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("loginname", "zhangfan");
        member1.put("password", "userpass");
        member1.put("email","10371443@qq.com");
        member1.put("sign_date", "2007-06-12");
//        buf.append("success_jsonpCallback(");
//        buf.append(member1.toString());
//        buf.append(")");
        buf.append("timeout!!!!!!!");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer,buf.toString().getBytes().length);
        logger.debug(buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private final class ReadTimeoutTask implements Runnable {
        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            //System.out.println("ReadTimeoutTask()");
            this.ctx = ctx;
        }

        public void run() {
            //System.out.println("run:"+ctx.channel().remoteAddress());
            if(this.ctx.channel().isOpen()) {
                long currentTime = System.currentTimeMillis();
                long nextDelay = WebServerTimeoutHandler.this.timeoutMillis - (currentTime - WebServerTimeoutHandler.this.lastReadTime);
                //System.out.println("timeoutMillis:"+timeoutMillis+" lastReadTime:"+lastReadTime+" currentTime:"+currentTime+" nextDelay:"+nextDelay);
                if(nextDelay <= 0L) {
                    //System.out.println("ReadTimeoutTask.run()<0");
                    WebServerTimeoutHandler.this.timeout = this.ctx.executor().schedule(this, WebServerTimeoutHandler.this.timeoutMillis, TimeUnit.MILLISECONDS);

                    try {
                        WebServerTimeoutHandler.this.readTimedOut(this.ctx);
                    } catch (Throwable var6) {
                        this.ctx.fireExceptionCaught(var6);
                    }
                } else {
                    //System.out.println("ReadTimeoutTask.run()>0");
                    WebServerTimeoutHandler.this.timeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}