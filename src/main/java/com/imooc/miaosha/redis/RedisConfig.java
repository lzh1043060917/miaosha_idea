package com.imooc.miaosha.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "redis") // 读application.property配置文件里以redis打头的配置
@Data
public class RedisConfig {
    // 这些配置好像都是自定义的
    private String host;
    private int port;
    private int timeout; // 秒
    private String password;
    private int poolMaxTotal;
    private int poolMaxIdle;
    private int poolMaxWait; // 秒
}
