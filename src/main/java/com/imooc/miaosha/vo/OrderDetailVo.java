package com.imooc.miaosha.vo;

import com.imooc.miaosha.domain.OrderInfo;

import lombok.Data;

@Data
public class OrderDetailVo {
    // 商品信息和订单信息
    private GoodsVo goods;

    private OrderInfo order;
}
