package com.imooc.miaosha.controller;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.OrderKey;
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

    private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();
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
b      * 收到请求，redis预减库存，库存不足直接返回，否则进入3
c      * 请求入队，立即返回排队中
d       * .请求出队，生成订单减库存
e       * .客户端轮询，是否秒杀成功
        * 优化后QPS：5311.929
        * 5000*10
    * * */
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaosha(Model model,
            // @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
            // @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String paramToken,
            MiaoshaUser user,
            @RequestParam("goodsId")long goodsId,
            @PathVariable("path")String path) {
        // model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if(over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 2.判断是否已经秒杀到了,不能重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            /*
             * 如果库存只有两个，A连续提交了两次，那redis库存会减为0，但是A的第二次提交会卡在重复秒杀上
             * 导致第二单不会成功。然后B提交一次，因为redis库存是0所以会告诉他秒杀完了，但是实际上其实还有一个商品，
             * 只不过没人可以买到了而已。这里感觉可以把预减的库存加回去，或者直接把这个逻辑加到预减库存之前。
             * */
            // redisService.incr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        // 预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
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

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
            @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    // 重置数据
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        // 删除用户的订单信息
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        // 删除秒杀完毕的信息
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }
    // 获取秒杀地址
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(@RequestParam("goodsId")long goodsId,
            MiaoshaUser user) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 生成Path
        String str = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(str);
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
            if (goods.getStockCount() > 0) {
                localOverMap.put(goods.getId(), false);
            } else {
                localOverMap.put(goods.getId(), true);
            }
        }
    }
}
