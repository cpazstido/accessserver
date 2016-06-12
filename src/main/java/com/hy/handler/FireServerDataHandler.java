package com.hy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class FireServerDataHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(FireServerDataHandler.class);

    private static final String CR = System.getProperty("line.separator");
    private int total = 0;
    private Boolean first;
    BufferedOutputStream bufferedOutputStream;
    private static int index = 0;
    private File file = null;

    public FireServerDataHandler() {
        logger.debug("FireServerDataHandler()");
        total = 0;
        first = true;
        bufferedOutputStream = null;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws IOException {
        System.out.println("channelInactive");
        bufferedOutputStream.close();
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("close");
        bufferedOutputStream.close();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("disconnect");
        bufferedOutputStream.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        if (first) {
            first = false;
            byte[] fileName = new byte[7];
            buffer.readBytes(fileName, 0, 7);
            logger.debug(new String(fileName));
            file = new File("d:\\test", new String(fileName));
            if (!file.exists()) {
                file.createNewFile();
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            } else {
                file = new File("d:\\test", index + new String(fileName));
                index++;
                file.createNewFile();
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                logger.debug("file is exists!");
            }
        }
        if (bufferedOutputStream != null) {
            int length = buffer.readableBytes();
            total += length;
            byte[] data = new byte[length];
            buffer.readBytes(data, 0, length);
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
        }
        logger.debug("total:" + total/1024.0 +"KB");
    }

    public void messageReceived(ChannelHandlerContext ctx, String msg)
            throws Exception {
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
