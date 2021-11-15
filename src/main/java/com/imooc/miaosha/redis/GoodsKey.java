package com.imooc.miaosha.redis;

public class GoodsKey extends BasePrefix {
    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public GoodsKey(String prefix) {
        super(prefix);
    }
    // 页面缓存有效期比较短，主要为了避免瞬时高流量，如果有效期太长，数据及时性会变差
    public static GoodsKey getGoodsList = new GoodsKey(60, "goods_list");
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "goods_detail");
    // 秒杀商品库存
    public static GoodsKey getMiaoshaGoodsStock= new GoodsKey(0, "goods_stock");
}
