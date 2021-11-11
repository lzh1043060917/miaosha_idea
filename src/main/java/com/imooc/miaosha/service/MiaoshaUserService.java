package com.imooc.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.dao.MiaoshaUserDao;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.exception.GlobalException;
import com.imooc.miaosha.redis.MiaoshaUserKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
    //
    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    private MiaoshaUserDao miaoshaUserDao;

    @Autowired
    private RedisService redisService;

    public MiaoshaUser getById(long id) {
        return miaoshaUserDao.getById(id);
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        // 第一次处理后的密码
        String formPass = loginVo.getPassword();
        // 判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        // 数据库里的密码
        String dbPass = user.getPassword();
        // 数据库里的盐值
        String saltDb = user.getSalt();
        // 计算出的pass
        String calcPass =  MD5Util.formPassToDBPass(formPass, saltDb);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        /*
        * 延长有效期的目的就是为了避免cookie中的token过期，假如：设置的token中的有效期是2个小时，
        * 那么用户在8点钟设置的这个token，10点钟就会失效。如果用户在9点的时候访问了系统，那么失效日期应该是11点而不是10点。
        * */
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {

        // 登陆成功，跳转到首页，给用户生成token，标识用户，写入cookie传给客户端，客户端
        // 每次访问上传cookie，服务端根据这个token获取用户信息
        // 生成cookie
        // key要包括token，值就是user对象
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        // 过期时间设置成redis的键值对的过期时间
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        // cookie加入客户端里面去
        response.addCookie(cookie);



    }
}
