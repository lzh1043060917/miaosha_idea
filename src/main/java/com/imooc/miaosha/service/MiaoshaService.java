package com.imooc.miaosha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    private static final String pathSalt = "abcd";


    // 事务操作
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        /*
        // 减库存,一个service如果要用其他dao的方法，最好调用其他service的方法。
        boolean success = goodsService.reduceStock(goods);

        // 下订单，实际写了俩表，orderInfo与miaosha_order
        OrderInfo orderInfo = orderService.createOrder(user, goods);
        return orderInfo;

         */


        //private void setGoodsOver(Long goodsId) {
            //redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
        //}


        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        if(success) {
            return orderService.createOrder(user, goods);
        }else {
            // setGoodsOver(goods.getId());
            return null;
        }

    }

    /**
     * 返回orderId：成功
     * 返回-1：秒杀失败
     * 返回0： 排队中
     * */
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if(order != null) {//秒杀成功
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if(isOver) {
                return -1;
            }else {
                return 0;
            }
        }
    }

    // 做标记，商品已经秒杀完了
    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(Long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
    }

    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() +
                "_" + goodsId, String.class);
        // 判断path是否相等
        return path.equals(pathOld);
    }

    // 生成秒杀路径
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + pathSalt);
        // path的有效期设置成60s，调用这个接口之后很快就会调用秒杀接口，每一个用户的path都可以是不同的
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_"+ goodsId, str);
        return str;
    }
}
