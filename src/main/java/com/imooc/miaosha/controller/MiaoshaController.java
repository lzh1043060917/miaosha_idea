package com.imooc.miaosha.controller;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private MQSender mqSender;
    /*
    * 优化前：QPS：1722
    * 5000*10
    * */
    /*
    * GET  POST区别：
    * get是幂等的：无论调用多少次，不会对服务端数据造成影响，因为是获取数据
    * post不是幂等的，向服务端提交数据，服务端数据会变化
    * */
    /*
    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> doMiaosha(Model model,
            // @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
            // @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String paramToken,
            MiaoshaUser user,
            @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 1.判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 2.判断是否已经秒杀到了,不能重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
            // 同一个商品已经有了一个订单，不能重复秒杀
        }
        // 3.减库存，下订单，写入秒杀订单，需要进行事务处理
        // 秒杀成功之后希望能直接进入订单详情页，需要一些商品信息以及订单信息
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);

    }
     */
    /*
    * 优化思路：
    * a.系统初始化，把商品库存数量加载入缓存
b      .收到请求，redis预减库存，库存不足直接返回，否则进入3
c      .请求入队，立即返回排队中
d       .请求出队，生成订单减库存
e       .客户端轮询，是否秒杀成功
    * */
    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaosha(Model model,
            // @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
            // @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String paramToken,
            MiaoshaUser user,
            @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
        if (stock < 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 2.判断是否已经秒杀到了,不能重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        // 入队
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setGoodsId(goodsId);
        miaoshaMessage.setUser(user);
        mqSender.sendMiaoshaMessage(miaoshaMessage);
        // 排队中
        return Result.success(0);
        /*
        // 1.判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 2.判断是否已经秒杀到了,不能重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
            // 同一个商品已经有了一个订单，不能重复秒杀
        }
        // 3.减库存，下订单，写入秒杀订单，需要进行事务处理
        // 秒杀成功之后希望能直接进入订单详情页，需要一些商品信息以及订单信息
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
         */

    }
    // 当类implements InitializingBean之后，项目启动之后，会回调这个接口
    // 把秒杀商品库存加入缓存
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        // 如果不为空，加载进缓存里面
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            // localOverMap.put(goods.getId(), false);
        }
    }
}
