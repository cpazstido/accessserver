package com.hy.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;

/**
 * Created by cpazstido on 2016/5/10.
 */
public class MyNettyEncoder extends MessageToByteEncoder<String> {
    private static Logger logger = Logger.getLogger("MyNettyEncoder");

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        try {
            byte[] bytes = msg.getBytes("UTF-8");
            int length = bytes.length;
            System.out.println("length:" + length);
//        byte[] header = LittleEndian.toLittleEndian(length); // int按小字节序转字节数组
//        out.writeBytes(header); // write header
            //logger.debug("cmd index:"+msg);

            out.writeBytes(("cmd index:" + msg).getBytes()); // write body
        }catch (Exception e){
            logger.error(e);
        }
    }
}
