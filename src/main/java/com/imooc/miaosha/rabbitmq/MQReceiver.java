package com.imooc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;
    /*
    // 监听这个队列，参数为队列名
    @RabbitListener(queues=MQConfig.QUEUE)
    public void receive(String message) {
        log.info("receive message" + message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info("topic  queue1 message:" + message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info("topic  queue2 message:" + message);
    }

    @RabbitListener(queues=MQConfig.HEADER_QUEUE)
    public void receiveHeaderQueue(byte[] message) {
        log.info(" header  queue message:"+new String(message));
    }
     */

    @RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaoshaQueue(String message) {
        // 还原出对象
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message, MiaoshaMessage.class);
        log.info("receive miaosha message" + message);
        MiaoshaUser miaoshaUser = miaoshaMessage.getUser();
        Long goodsId = miaoshaMessage.getGoodsId();
        // 预减库存和查看有无重复秒杀，已经把大部分请求拦住了
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        // 这个数字是数据库里的数字，准确的
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        // 之后就是生成订单减库存那套了，相当于把原先同步进行的操作变成异步
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(miaoshaUser, goods);
    }
}
