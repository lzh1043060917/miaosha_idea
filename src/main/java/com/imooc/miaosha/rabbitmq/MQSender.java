package com.imooc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.redis.RedisService;

@Service
public class MQSender {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    // 操作的工具类
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void send(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send message" + msg);
        // 队列名称, 数据
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }
}
