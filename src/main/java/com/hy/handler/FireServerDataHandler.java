package com.hy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class FireServerDataHandler extends SimpleChannelInboundHandler<String> {

    private static final String CR = System.getProperty("line.separator");
    private int total = 0;
    private Boolean first;
    BufferedOutputStream bufferedOutputStream;
    private static int index = 0;
    private File file = null;

    public FireServerDataHandler() {
        System.out.println("FireServerDataHandler()");
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
            System.out.println(new String(fileName));
            file = new File("d:\\test", new String(fileName));
            if (!file.exists()) {
                file.createNewFile();
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            } else {
                file = new File("d:\\test", index + new String(fileName));
                index++;
                file.createNewFile();
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                System.out.println("file is exists!");
            }
        }
        if (bufferedOutputStream != null) {
            int length = buffer.readableBytes();
            byte[] data = new byte[length];
            buffer.readBytes(data, 0, length);
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
        }
        total += buffer.readableBytes();
        System.out.println("total:" + total);
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
