package com.imooc.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.UserKey;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.UserService;

@SuppressWarnings("checkstyle:RegexpSingleline")
@Controller
@RequestMapping("/demo")
public class SampleController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender sender;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
    model.addAttribute("name", "lzh");
    return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        return Result.success(user);
    }
    // 事务
    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User  var  = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(var);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user  = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById, "" + 1, user); //UserKey:id1
        return Result.success(true);
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        // sender.sendTopic("hello,imooc");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout() {
        // sender.sendFanout("hello,imooc");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader() {
        // sender.sendHeader("hello,imooc");
        return Result.success("Hello，world");
    }
}
