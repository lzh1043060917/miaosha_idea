package com.imooc.miaosha.vo;

import javax.validation.constraints.NotNull;

import com.imooc.miaosha.validator.IsMobile;

import lombok.Data;

@Data // 接收前端参数的类
public class LoginVo {
    // 自定义参数校验IsMobile
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    private String password;
}
