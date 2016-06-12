package com.hy.test;

import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by cpazstido on 2016/6/7.
 */
public class SubscribeListener extends JedisPubSub {
    private static Logger logger = Logger.getLogger(SubscribeListener.class);

    // 取得订阅的消息后的处理
    public void onMessage(String channel, String message) {
        logger.debug(channel + "=" + message);
        //**************取消订阅****************
        //**************取消订阅****************
        //**************取消订阅****************
        //this.unsubscribe("fire");
    }

    // 初始化订阅时候的处理
    public void onSubscribe(String channel, int subscribedChannels) {
        logger.debug(channel + "=" + subscribedChannels);
    }

    // 取消订阅时候的处理
    public void onUnsubscribe(String channel, int subscribedChannels) {
        logger.debug(channel + "=" + subscribedChannels);
    }

    // 初始化按表达式的方式订阅时候的处理
    public void onPSubscribe(String pattern, int subscribedChannels) {
        logger.debug(pattern + "=" + subscribedChannels);
    }

    // 取消按表达式的方式订阅时候的处理
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        logger.debug(pattern + "=" + subscribedChannels);
    }

    // 取得按表达式的方式订阅的消息后的处理
    public void onPMessage(String pattern, String channel, String message) {
        logger.debug(pattern + "=" + channel + "=" + message);
    }
}
