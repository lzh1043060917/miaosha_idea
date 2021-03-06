package com.imooc.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisPoolFactory {

    /*
     * 之所以会报错循环引用是因为：在构造RedisService的时候，按照成员变量声明的顺序，
     * 容器首先会去注入JedisPool，
     * 而JedisPool又需要注入RedisConfig，此时，RedisService中的RedisConfig还没有注入进来，就报错了。
     * 其实，只需要简单的调整下JedisPool和RedisConfig的声明顺序也是可以的，
     * 这样就保证了在注入JedisPool的时候，已经注入了RedisConfig
     * */
    @Autowired
    private RedisConfig redisConfig;

    // 注入一个bean，bean加载到容器里面
    @Bean
    public JedisPool jedisPoolFactory() {
        // 连接池要有个config对象
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        // maxTotal是连接池中总连接的最大数量
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        // 秒转化成毫秒
        // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000);
        JedisPool jp = new JedisPool(poolConfig, redisConfig.getHost(),
                redisConfig.getPort(), redisConfig.getTimeout() * 1000, redisConfig.getPassword(),
                0);
        return jp;
    }
}
