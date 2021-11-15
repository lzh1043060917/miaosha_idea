package com.imooc.miaosha.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";

    /**
     * Direct模式，交换机Exchange，创建消息队列。指定队列名，往里面塞数据取数据。
     * */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }
}
