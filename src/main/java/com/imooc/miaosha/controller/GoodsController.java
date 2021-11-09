package com.imooc.miaosha.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping(value="/to_list")
    public String list(Model model,
            // @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
            // @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String paramToken,
            MiaoshaUser user,
            HttpServletResponse response) {
        /*
        * 这么做是因为，到了商品详情页等页面，也需要用户信息，也需要cookieToken等参数，写下面的
        * 逻辑，UserArgumentResolver其实就是做了下面的这个逻辑，节省代码。
        * */
        /*
        这些逻辑放到UserArgumentResolver里面了
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        MiaoshaUser user = userService.getByToken(response, token);
        model.addAttribute("user", user);*/
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        List<GoodsVo>  goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        return "goods_list";
    }
    // 跳转到商品详情页
    @RequestMapping(value="/to_detail/{goodsId}")
    public String detail(Model model,MiaoshaUser user,
            @PathVariable("goodsId")long goodsId) {
        model.addAttribute("user", user);
        // snowflake
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        // 开始时间转化为毫秒
        long startAt = goods.getStartDate().getTime();
        // 结束时间转化为毫秒
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        // 秒杀状态，根据它显示不同文案
        int miaoshaStatus = 0;
        // 离秒杀开始还有多久
        int remainSeconds = 0;
        if (now < startAt) {
            // 秒杀没开始，倒计时
            miaoshaStatus = 0;
            // 转化成秒
            remainSeconds = (int)((startAt - now )/1000);
        } else if (now > endAt) {
            // 秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {
            // 正在进行
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }



}
