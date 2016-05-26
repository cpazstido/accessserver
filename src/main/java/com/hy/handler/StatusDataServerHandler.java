package com.hy.handler;

import com.hy.SimulateDevice.CommonFunctions;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;

/**
 * Created by cpazstido on 2016/5/26.
 */
public class StatusDataServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        int length = datagramPacket.content().readableBytes();
        byte[] bb = new byte[length];
        datagramPacket.content().readBytes(bb);
        System.out.println("length:"+length+" contents:" + CommonFunctions.byteToHexStr(bb, bb.length));
    }
}
