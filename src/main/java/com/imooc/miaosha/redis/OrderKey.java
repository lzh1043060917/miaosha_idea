package com.imooc.miaosha.redis;

public class OrderKey extends BasePrefix {
    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    // userId与goodsId,过期时间设置成10天吧,教程里面是永久
    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey(0, "orderExists");

    public OrderKey(String prefix) {
        super(prefix);
    }
}
