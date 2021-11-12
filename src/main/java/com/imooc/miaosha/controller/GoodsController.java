package com.imooc.miaosha.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.GoodsDetailVo;
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

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    /*
    * 优化前：
    * QPS:2896.536
    * 5000 * 10
    *
    * 优化后
    * QPS:5029.2
    * 5000*10
    * */
    // produces = "text/html"表明该方法处理产生html之类的数据
    @RequestMapping(value="/to_list", produces = "text/html")
    // 接口返回一个json数据
    @ResponseBody
    public String list(Model model,
            // @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
            // @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String paramToken,
            MiaoshaUser user,
            HttpServletRequest request,
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
        /*
        if (user == null) {
            return "login";
        }*/
        // 取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user", user);
        List<GoodsVo>  goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        // return "goods_list";
        WebContext ctx = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        // 取不到，手动渲染,参数是模板名称（goods_list）与context
        // 找到IContext接口，选中，ctrl + H看所有实现
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            // 把手动渲染的结果保存进缓存
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        // 返回html页面
        return html;
    }
    // 跳转到商品详情页
    // 页面静态化,服务端只需要提供数据。
    /*
    * thymeleaf是服务端模板，分离之前是服务端查询数据库，加载thymeleaf模板，
    * 把数据填充到thymeleaf模板里面，生成完整的html，然后把完整的html发送给浏览器展示。
    * 分离以后服务端只需要输出数据，不需要输出html。html可以放在单独的静态文件服务器上，
    * 浏览器访问的是静态服务器上的html，拿到html以后，使用js去调用服务端的接口，拿到数据，填充页面进行展示。
    * */
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(Model model,MiaoshaUser user,
            @PathVariable("goodsId")long goodsId,
            HttpServletRequest request,
            HttpServletResponse response) {
        model.addAttribute("user", user);

        // snowflake
        // 手动渲染，先取数据
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
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setUser(user);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        // 这个参数记得
        goodsDetailVo.setRemainSeconds(remainSeconds);
        return Result.success(goodsDetailVo);
    }

    // 跳转到商品详情页，页面缓存
    @RequestMapping(value="/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(Model model,MiaoshaUser user,
            @PathVariable("goodsId")long goodsId,
            HttpServletRequest request,
            HttpServletResponse response) {
        model.addAttribute("user", user);
        // 取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        // snowflake
        // 手动渲染，先取数据
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

        WebContext ctx = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        // 取不到，手动渲染,参数是模板名称（goods_list）与context
        // 找到IContext接口，选中，ctrl + H看所有实现
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            // 把手动渲染的结果保存进缓存
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        // 返回html页面
        return html;
        // return "goods_detail";
    }

}
