package com.hy.test;

import com.hy.utils.RedisUtil;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * Created by cpazstido on 2016/6/7.
 */
public class JedisPublisherTest {
    private static Logger logger = Logger.getLogger(JedisPublisherTest.class);
    public static void main(String[] args) {
        Jedis jedis = RedisUtil.getJedis();
        logger.debug(jedis.publish("fire","hiiiiiiiiiiiiiiiiiiii"));
    }
}
