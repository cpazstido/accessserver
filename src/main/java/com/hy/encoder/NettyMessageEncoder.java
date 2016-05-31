package com.hy.encoder;

import com.hy.bean.NettyMessage;
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
            if(nettyMessage == null || nettyMessage.getBody() == null){
            }
            else{
                byteBuf.writeBytes((byte[]) nettyMessage.getBody());
            }
        }catch (Exception e){
            logger.error(e);
        }
    }
}
