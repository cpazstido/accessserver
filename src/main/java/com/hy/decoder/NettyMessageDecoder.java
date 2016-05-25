package com.hy.decoder;

import com.hy.bean.Header;
import com.hy.bean.NettyMessage;
import com.hy.utils.BigEndian;
import com.hy.utils.LittleEndian;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by cpazstido on 2016/5/20.
 */
public class NettyMessageDecoder extends ByteArrayDecoder {
    private static Logger logger = Logger.getLogger(NettyMessageDecoder.class);
    public NettyMessageDecoder(){
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            // 如果没有接收完Header部分（4字节），直接退出该方法
            if (in.readableBytes() >= 14) {
                // 标记开始位置，如果一条消息没传输完成则返回到这个位置
                in.markReaderIndex();
                //Flag 4
                byte[] bytesFlag = new byte[4];
                in.readBytes(bytesFlag); // 读取4字节的flag
//                logger.debug("flag:" + new String(bytesFlag));

                //Version 1
                byte[] bytesVersion = new byte[1];
                in.readBytes(bytesVersion);
//                logger.debug("Version:" + (int)bytesVersion[0]);

                //index
                byte[] bytesIndex = new byte[4];
                in.readBytes(bytesIndex);
//                logger.debug("Index:" + BigEndian.getBigEndianInt(bytesIndex));

                //Type 1
                byte[] bytesType = new byte[1];
                in.readBytes(bytesType);

                //Length 4
                byte[] bytesLength = new byte[4];
                in.readBytes(bytesLength);
                int bodyLength = BigEndian.getBigEndianInt(bytesLength);
//                logger.debug("length:" + bodyLength);

                //System.out.println("Index:" + LittleEndian.getLittleEndianInt(bytesIndex));
                //System.out.println("Type:" + (int)bytesType[0]);
                //System.out.println("Length:" + LittleEndian.getLittleEndianInt(bytesLength));
                //System.out.println("readableBytes:" + in.readableBytes() + "  bodyLength:" + bodyLength);

                byte[] dataBytes = new byte[bodyLength + 14];
                System.arraycopy(bytesFlag, 0, dataBytes, 0, 4);
                System.arraycopy(bytesVersion, 0, dataBytes, 4, 1);
                System.arraycopy(bytesIndex, 0, dataBytes, 5, 4);
                System.arraycopy(bytesType, 0, dataBytes, 9, 1);
                System.arraycopy(bytesLength, 0, dataBytes, 10, 4);

                // 如果body没有接收完整
                if (in.readableBytes() < bodyLength) {
                    in.resetReaderIndex(); // ByteBuf回到标记位置
                } else {
                    byte[] bodyBytes = new byte[bodyLength];
                    in.readBytes(bodyBytes);
                    System.arraycopy(bodyBytes, 0, dataBytes, 14, bodyLength);
                    String body = new String(bodyBytes, "UTF-8");
                    NettyMessage message = new NettyMessage();
                    Header header = new Header();

                    header.setFlag(bytesFlag);
                    header.setVersion(bytesVersion[0]);
                    header.setIndex(BigEndian.getBigEndianInt(bytesIndex));
                    header.setTypes(bytesType[0]);
                    header.setLen(BigEndian.getBigEndianInt(bytesLength));
                    message.setHeader(header);
                    message.setBody(body);
//                    logger.debug("Body:"+body);
                    out.add(message); // 解析出一条消息
                }
            }
        } catch (Exception e) {
            logger.error("bad data formate,the socket will be closed! " + e);
            ctx.channel().close();
        }
    }
}
