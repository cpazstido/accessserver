package com.hy.handler;

import com.hy.bean.Header;
import com.hy.bean.MessageTypeReq;
import com.hy.bean.NettyMessage;
import com.hy.resolver.FireDataResolver;
import com.hy.utils.PropertyUtils;
import com.hy.utils.RedisUtil;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import redis.clients.jedis.Jedis;

/**
 * Created by cpazstido on 2016/5/25.
 */
public class HeartBeatTask implements Runnable {
    private static Logger logger = Logger.getLogger(HeartBeatTask.class);
    private final FireServerHandler fireServerHandler;


    public HeartBeatTask(final FireServerHandler fireServerHandler) {
        this.fireServerHandler = fireServerHandler;
    }

    public void run() {
        try {
            if (fireServerHandler.LoseHeartbeatTimes < Integer.parseInt(PropertyUtils.getValue("LoseHeartbeatTimes"))) {
                synchronized (this) {
                    fireServerHandler.LoseHeartbeatTimes++;
//                    logger.debug(fireServerHandler.channelHandlerContext.channel().remoteAddress() + " LoseHeartbeatTimes++:" + fireServerHandler.LoseHeartbeatTimes);
                }
                NettyMessage hearbeatMessage = buildHearbeatMessage();
                fireServerHandler.channelHandlerContext.writeAndFlush(hearbeatMessage);
            } else {
                logger.error("lost heart beat " + Integer.parseInt(PropertyUtils.getValue("LoseHeartbeatTimes")) + "times,close the socket!");
                fireServerHandler.fireClients.remove(fireServerHandler.deviceId);
                fireServerHandler.channelHandlerContext.close();
                fireServerHandler.heartBeatSchedule.cancel(true);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private NettyMessage buildHearbeatMessage() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setLen(0);
        header.setFlag("HYVC".getBytes());
        header.setIndex(0);
        header.setVersion((byte) 1);
        header.setTypes(MessageTypeReq.HEARTBEAT_REQ.value());
        message.setHeader(header);
        return message;
    }
}