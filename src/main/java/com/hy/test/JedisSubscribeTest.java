package com.hy.test;

import com.hy.utils.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * Created by cpazstido on 2016/6/7.
 */
public class JedisSubscribeTest {
    public static void main(String[] args) {
        SubscribeListener subscribeListener = new SubscribeListener();
        Jedis jedis = RedisUtil.getJedis();
        jedis.subscribe(subscribeListener,"fire","break");
    }
}
