package com.imooc.miaosha.redis;

public class UserKey extends BasePrefix {
    private UserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    private UserKey(String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey(30, "id");

    public static UserKey getByName = new UserKey(30, "name");
}
