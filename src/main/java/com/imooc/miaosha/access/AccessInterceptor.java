package com.imooc.miaosha.access;

import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.AccessKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;

// 拦截器
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    private RedisService redisService;
    // 在controller之前执行
    /*
    * 当进入拦截器链中的某个拦截器，并执行preHandle方法后
1.当preHandle方法返回false时，从当前拦截器往回执行所有拦截器的afterCompletion方法，
* 再退出拦截器链。也就是说，请求不继续往下传了，直接沿着来的链往回跑。
2.当preHandle方法全为true时，执行下一个拦截器,直到所有拦截器执行完。再运行被拦截的Controller。
* 然后进入拦截器链，运行所有拦截器的postHandle方法,完后从最后一个拦截器往回执行所有拦截器的afterCompletion方法.
    * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 感觉目前的逻辑是，如果没有AccessLimit注解，就直接执行controller；
        // 如果有AccessLimit注解，需要登录但是没登录，会报错；
        // 有AccessLimit注解就会执行限流。
        // 那要是一个接口需要登录，但是不需要限流，没注解，就会无法校验是否登录了，所以感觉还是
        // 要有两个注解，一个校验登录，一个校验限流，如果没有登录的那个注解，就返回true。执行下一个限流校验。
        if (handler instanceof HandlerMethod) {
            MiaoshaUser user = getUser(request, response);
            // 获取到user之后，存储到ThreadLocal里面
            // 这里看一下，set会覆盖旧值，所以这里不用判断是否为空
            UserContext.setUser(user);
            // 可以拿到方法和方法上的注解
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 拿到controller上的accessLimit注解
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            // 如果没有限流，则不做下面的处理,执行下一个拦截器，之后执行controller
            if (Objects.isNull(accessLimit)) {
                return true;
            }
            // 过期时间
            int seconds = accessLimit.second();
            // 时间内的次数
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.neeedLogin();

            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    // 需要登录但获取对象失败，则返回错误
                    render(response, CodeMsg.SESSION_ERROR);
                    // 不走controller
                    return false;
                }
                key += "_" + user.getId();
            } else {
                // 不需要登录，则key不加上userId
            }
            // 防刷,查询访问的次数,五秒钟之内,某个用户，访问当前url，限制访问5次
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) {
                // 第一次访问，设置访问次数初始值
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(ak, key);
            } else {
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
        // return super.preHandle(request, response, handler);
    }

    private void render(HttpServletResponse response, CodeMsg sessionError) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        // 拿到输出流
        OutputStream out = response.getOutputStream();
        // 转换成string，写出去
        String str = JSON.toJSONString(Result.error(sessionError));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        // 根据参数名获得参数
        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(cookieToken) ? paramToken : cookieToken;
        // 根据token获取秒杀user对象
        MiaoshaUser user = userService.getByToken(response, token);
        return user;
    }

    // 遍历request里的所有cookie，找到想要的那个
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        // 避免测试的时候报错。应该是测试tolist的时候，user没有值，所以报错了
        if (cookies == null || StringUtils.isEmpty(cookieName)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
