package com.imooc.miaosha.vo;

import lombok.Data;

@Data
public class LoginVo {
    // 接收前端参数的类
    private String mobile;

    private String password;
}
