package com.imooc.miaosha.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.service.MiaoshaUserService;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    private RedisService redisService;

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
        return "goods_list";
    }


}
