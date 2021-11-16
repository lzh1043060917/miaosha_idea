package com.imooc.miaosha.redis;

public class MiaoshaKey extends BasePrefix {
    private MiaoshaKey(String prefix) {
        super(prefix);
    }
    private MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    // 设置成永久存在吧,标记商品减库存失败，秒杀已经结束
    public static MiaoshaKey isGoodsOver = new MiaoshaKey(0, "isGoodsOver");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "miaoshaPath");
}
