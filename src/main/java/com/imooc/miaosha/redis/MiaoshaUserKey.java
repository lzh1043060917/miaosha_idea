package com.imooc.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix {

    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

    private String prefix ;

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");
    // 根据手机号查找用户，缓存
    public static MiaoshaUserKey getById = new MiaoshaUserKey(3600 * 24 * 2, "id");

    public MiaoshaUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public MiaoshaUserKey(String prefix) {
        super(prefix);
    }
}
