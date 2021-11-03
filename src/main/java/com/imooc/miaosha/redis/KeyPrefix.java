package com.imooc.miaosha.redis;

public interface KeyPrefix {
    // 接口，抽象类，实现类三层
    // 过期时间
    public int expireSeconds();
    // 前缀
    public String getPrefix();
}
