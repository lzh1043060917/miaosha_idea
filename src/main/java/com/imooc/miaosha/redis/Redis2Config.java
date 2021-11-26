package com.imooc.miaosha.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "redis2")
@Data
public class Redis2Config {
    private String host;
    private int port;
    private int timeout; // 秒
    private String password;
    private int poolMaxTotal;
    private int poolMaxIdle;
    private int poolMaxWait; // 秒
}
