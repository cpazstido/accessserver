package com.hy.encoder;

import com.hy.bean.NettyMessage;
import com.hy.utils.BigEndian;
import com.hy.utils.ByteHelper;
import com.hy.utils.CommonFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by cpazstido on 2016/5/24.
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {
    private static Logger logger = Logger.getLogger(NettyMessageEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, ByteBuf byteBuf) throws Exception {
        try{
            if (nettyMessage == null || nettyMessage.getHeader() == null)
                throw new Exception("The encode message header is null");

            byteBuf.writeBytes(nettyMessage.getHeader().getFlag());
            byteBuf.writeByte(nettyMessage.getHeader().getVersion());
            byteBuf.writeInt(nettyMessage.getHeader().getIndex());
            byteBuf.writeByte(nettyMessage.getHeader().getTypes());
            byteBuf.writeInt(nettyMessage.getHeader().getLen());
            byte mes[] = new byte[14+nettyMessage.getHeader().getLen()];
            System.arraycopy(nettyMessage.getHeader().getFlag(),0,mes,0,4);
            byte version[] = new byte[1];
            version[0] = nettyMessage.getHeader().getVersion();
            System.arraycopy(version,0,mes,4,1);
            System.arraycopy(BigEndian.toBigEndian(nettyMessage.getHeader().getIndex()),0,mes,5,4);
            byte types[] = new byte[1];
            types[0] = nettyMessage.getHeader().getTypes();
            System.arraycopy(types,0,mes,9,1);
            System.arraycopy(BigEndian.toBigEndian(nettyMessage.getHeader().getLen()),0,mes,10,4);

            if(nettyMessage == null || nettyMessage.getBody() == null){
            }
            else{
                byteBuf.writeBytes((byte[]) nettyMessage.getBody());
                System.arraycopy((byte[])nettyMessage.getBody(),0,mes,14,((byte[])((byte[]) nettyMessage.getBody())).length);
            }
            logger.debug("向"+channelHandlerContext.channel().remoteAddress()+"发送:"+ CommonFunctions.byteToHexStr(mes,14+nettyMessage.getHeader().getLen()));
        }catch (Exception e){
            logger.error(e);
        }
    }
}
