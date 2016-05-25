package com.hy.decoder;

import com.hy.utils.LittleEndian;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by cpazstido on 2016/5/10.
 */
public class MyNettyDecoder extends ByteArrayDecoder {
    private static Logger logger = Logger.getLogger(MyNettyDecoder.class);
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //System.out.println(in.readableBytes()+"===========");
        try {
            // 如果没有接收完Header部分（4字节），直接退出该方法
            if (in.readableBytes() >= 33) {
                // 标记开始位置，如果一条消息没传输完成则返回到这个位置
                in.markReaderIndex();
                //Flag 4
                byte[] bytesFlag = new byte[4];
                in.readBytes(bytesFlag); // 读取4字节的flag
                //System.out.println("flag:" + new String(bytesFlag));

                //Version 1
                byte[] bytesVersion = new byte[1];
                in.readBytes(bytesVersion);
                //System.out.println("Version:" + (int)bytesVersion[0]);

                //DeviceId 17
                byte[] bytesDeviceId = new byte[17];
                in.readBytes(bytesDeviceId);

                //frameType 1
                byte[] bytesFrameType = new byte[1];
                in.readBytes(bytesFrameType);

                //packetType 1
                byte[] bytesPacketType = new byte[1];
                in.readBytes(bytesPacketType);

                //frameNo 4
                byte[] bytesFrameNO = new byte[4];
                in.readBytes(bytesFrameNO);

                //transformat 1
                byte[] bytesTransformat = new byte[1];
                in.readBytes(bytesTransformat);

                //packetLength 4
                byte[] bytesPacketLength = new byte[4];
                in.readBytes(bytesPacketLength);
                int bodyLength = LittleEndian.getLittleEndianInt(bytesPacketLength);

                //System.out.println("Index:" + LittleEndian.getLittleEndianInt(bytesIndex));
                //System.out.println("Type:" + (int)bytesType[0]);
                //System.out.println("Length:" + LittleEndian.getLittleEndianInt(bytesLength));
                //System.out.println("readableBytes:" + in.readableBytes() + "  bodyLength:" + bodyLength);

                byte[] dataBytes = new byte[bodyLength + 33];
                System.arraycopy(bytesFlag, 0, dataBytes, 0, 4);
                System.arraycopy(bytesVersion, 0, dataBytes, 4, 1);
                System.arraycopy(bytesDeviceId, 0, dataBytes, 5, 17);
                System.arraycopy(bytesFrameType, 0, dataBytes, 22, 1);
                System.arraycopy(bytesPacketType, 0, dataBytes, 23, 1);
                System.arraycopy(bytesFrameNO, 0, dataBytes, 24, 4);
                System.arraycopy(bytesTransformat, 0, dataBytes, 28, 1);
                System.arraycopy(bytesPacketLength, 0, dataBytes, 29, 4);

                // 如果body没有接收完整
                if (in.readableBytes() < bodyLength) {
                    in.resetReaderIndex(); // ByteBuf回到标记位置
                } else {
                    byte[] bodyBytes = new byte[bodyLength];
                    in.readBytes(bodyBytes);
                    System.arraycopy(bodyBytes, 0, dataBytes, 33, bodyLength);
                    String body = new String(dataBytes, "UTF-8");
                    out.add(dataBytes); // 解析出一条消息
                }
            }
        } catch (Exception e) {
            logger.error(e);
            ctx.channel().close();
        }
    }
}