package com.imooc.miaosha.domain;

import java.util.Date;

import lombok.Data;

@Data
public class MiaoshaUser {
    // 和数据库表中对应的对象
    private Long id;
    private String nickname;
    private String password;
    private String salt;
    private String head;
    private Date registerDate;
    private Date lastLoginDate;
    private Integer loginCount;
}
