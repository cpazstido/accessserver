package com.hy.handler;

import com.hy.bean.Header;
import com.hy.bean.MessageTypeReq;
import com.hy.bean.NettyMessage;
import com.hy.resolver.FireDataResolver;
import com.hy.utils.PropertyUtils;
import com.hy.utils.RedisUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cpazstido on 2016/5/24.
 */
public class LoginChallengeTask implements Runnable {
    private static Logger logger = Logger.getLogger(LoginChallengeTask.class);
    private final FireServerHandler fireServerHandler;
    private final String randomCode;

    public LoginChallengeTask(final FireServerHandler fireServerHandler, String randomCode) {
        this.fireServerHandler = fireServerHandler;
        this.randomCode = randomCode;
        logger.debug("randomCode:" + randomCode);
    }

    public void run() {
        try {
            if (fireServerHandler.loginChangeTimes > (Integer.parseInt(PropertyUtils.getValue("LoginChallengeTimes"))-1) && fireServerHandler.loginChangeSchedule != null) {
                fireServerHandler.loginChangeSchedule.cancel(true);
                fireServerHandler.loginChangeSchedule = null;
                FireDataResolver fireDataResolver = new FireDataResolver();
                Jedis jedis = RedisUtil.getJedis();
                jedis.set(fireServerHandler.ip,fireServerHandler.ip);
                jedis.expire(fireServerHandler.ip, Integer.parseInt(PropertyUtils.getValue("BlacklistTimeout")));
                fireServerHandler.channelHandlerContext.writeAndFlush(fireDataResolver.buildInfoResp("3 times did't response,already in blacklist,please login later!")).addListener(ChannelFutureListener.CLOSE);
            } else {
                NettyMessage loginChange = buildLoginChange();
                fireServerHandler.loginChangeTimes++;
//                logger.debug(new String((byte [])loginChange.getBody()));
                fireServerHandler.channelHandlerContext.writeAndFlush(loginChange);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private NettyMessage buildLoginChange() {
        NettyMessage message = new NettyMessage();
        String body = writeXml();
        Header header = new Header();
        header.setLen(body.length());
        header.setFlag("HYVC".getBytes());
        header.setIndex(0);
        header.setVersion((byte) 1);
        header.setTypes(MessageTypeReq.LOGIN_REQ.value());
        message.setHeader(header);
        message.setBody(writeXml().getBytes());
        return message;
    }

    public String writeXml() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType","loginchange");
        Element eRandomCode = root.addElement("randomCode");
        eRandomCode.setText(randomCode);
        return document.asXML();
    }
}
