package com.imooc.miaosha.redis;

public abstract class BasePrefix implements KeyPrefix {

    private int expireSeconds;

    private String prefix;

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }
    // 1
    public BasePrefix(String prefix) { //0代表永不过期
        this(0, prefix);
    }

    public int expireSeconds() {
        return expireSeconds;
    }

    public String getPrefix() {
        // 同类名作为一个前缀
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
