package com.imooc.miaosha.redis;

public class OrderKey extends BasePrefix {
    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    // userId与goodsId,过期时间设置成五天吧
    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey(3600 * 24 * 5, "moug");

    public OrderKey(String prefix) {
        super(prefix);
    }
}
