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
    // 注入AmqpTemplate接口，该接口定义了发送和接收消息的基本操作
    @Autowired
    private AmqpTemplate amqpTemplate;
    /*
    public void send(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send message" + msg);
        // 第一个参数是发送到的队列名称，第二个参数是发送的数据
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }

    public void sendTopic(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send topic message:"+msg);
        // 第一个参数指将消息发送到该名称的交换机，第二个参数为对应的routing_key,第三个参数为发送的具体消息
        // 下方第一个消息可以被两个队列收到，下方第二个消息只能匹配topic.#，所以只能被队列2收到
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg+"1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg+"2");
    }
    // 因为是广播模式，队列1，队列2都能收到消息
    public void sendFanout(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send fanout message:"+msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
    }

    public void sendHeader(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send header message:"+msg);
        // header
        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");
        // message，需要用字节方式传输
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }
     */

    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage) {
        String msg = RedisService.beanToString(miaoshaMessage);
        log.info("send message" + msg);
        // 第一个参数是发送到的队列名称，第二个参数是发送的数据
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
    }
}
