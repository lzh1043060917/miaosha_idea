package com.imooc.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisPool2Factory {
    @Autowired
    private Redis2Config redis2Config;

    // 注入一个bean，bean加载到容器里面
    @Bean
    public JedisPool jedisPool2Factory() {
        // 连接池要有个config对象
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        poolConfig.setMaxIdle(redis2Config.getPoolMaxIdle());
        // maxTotal是连接池中总连接的最大数量
        poolConfig.setMaxTotal(redis2Config.getPoolMaxTotal());
        // 秒转化成毫秒
        // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        poolConfig.setMaxWaitMillis(redis2Config.getPoolMaxWait() * 1000);
        JedisPool jp = new JedisPool(poolConfig, redis2Config.getHost(),
                redis2Config.getPort(), redis2Config.getTimeout() * 1000, redis2Config.getPassword(),
                0);
        return jp;
    }
}
