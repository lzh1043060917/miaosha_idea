package com.imooc.miaosha.vo;

import com.imooc.miaosha.domain.MiaoshaUser;

import lombok.Data;

@Data
public class GoodsDetailVo {
    private int miaoshaStatus;

    private int remainSeconds;

    private GoodsVo goods ;

    private MiaoshaUser user;
}
