package com.imooc.miaosha.rabbitmq;

import com.imooc.miaosha.domain.MiaoshaUser;

import lombok.Data;

@Data
public class MiaoshaMessage {
    private MiaoshaUser user;

    private long goodsId;
}
