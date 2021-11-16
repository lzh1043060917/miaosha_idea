package com.imooc.miaosha.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADER_QUEUE = "header.queue";
    public static final String TOPIC_EXCHANGE = "topicExchage";
    public static final String FANOUT_EXCHANGE = "fanoutxchage";
    public static final String HEADERS_EXCHANGE = "headersExchage";

    /**
     * Direct模式，交换机Exchange，创建消息队列。指定队列名，往里面塞数据取数据。
     * 消息发到交换机exchange，再到队列
     */
    @Bean
    public Queue queue() {
        // Queue的第一个参数为队列名称，第二个参数为是否持久存在
        return new Queue(QUEUE, true);
    }

    /**
     * Topic模式，交换机exchange
     * 将路由键和某模式进行匹配。此时队列需要绑定要一个模式上。符号“#”匹配一个或多个词，
     * 符号“.”匹配不多不少一个词。因此“abc.#”能够匹配到“abc.def.ghi”，但是“abc.” 只会匹配到“abc.def”。
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }

    @Bean // 交换机
    public TopicExchange topicExchage() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    // 路由键的作用：匹配队列的
    @Bean // 将topicQueue1与topicExchange交换机绑定
    public Binding topicBinding1() { // 如果路由键为topic.key1，会被发送到TOPIC_QUEUE1队列
        return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with("topic.key1");
    }

    @Bean // 将topicQueue2与topicExchange交换机绑定
    public Binding topicBinding2() { // 如果路由键为topic.#，会被发送到TOPIC_QUEUE2队列
        return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with("topic.#");
    }
    /*
    * 广播模式,fanout
    * */
    @Bean // 交换机
    public FanoutExchange fanoutExchage(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }
    // 队列1，2绑定到同一个交换机
    @Bean
    public Binding FanoutBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchage());
    }
    @Bean
    public Binding FanoutBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchage());
    }

    /**
     * Header模式 交换机Exchange
     * */
    @Bean
    public HeadersExchange headersExchage(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    @Bean
    public Queue headerQueue1() {
        return new Queue(HEADER_QUEUE, true);
    }
    @Bean
    public Binding headerBinding() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        // headers必须完全满足上面那俩key和value，才能送到队列里面去
        return BindingBuilder.bind(headerQueue1()).to(headersExchage()).whereAll(map).match();
    }

    @Bean
    public Queue miaoshaQueue() {
        // Queue的第一个参数为队列名称，第二个参数为是否持久存在
        return new Queue(MIAOSHA_QUEUE, true);
    }
}
