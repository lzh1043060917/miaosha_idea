package com.imooc.miaosha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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

    private static char[] ops = new char[] {'+', '-', '*'};


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

    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <= 0) {
            return null;
        }
        int width = 100;
        int height = 32;
        //create the image
        // 内存里的图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        // 设置背景颜色
        g.setColor(new Color(0xDCDCDC));
        // 填充
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        // 设置矩形框
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        // 生成50个点
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        // 设置验证码的颜色与字体
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //计算验证码的计算结果，并且把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }
    // 计算表达式结果
    private int calc(String verifyCode) {
        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
            return (Integer)engine.eval(verifyCode);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String generateVerifyCode(Random rdm) {
        // 生成三个数，再随机选择俩运算符号，组成一个表达式
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }
}
