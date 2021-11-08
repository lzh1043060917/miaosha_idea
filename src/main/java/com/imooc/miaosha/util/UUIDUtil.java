package com.imooc.miaosha.util;

import java.util.UUID;

public class UUIDUtil {
    public static String uuid() {
        // 去掉原生uuid上面的-
        return UUID.randomUUID().toString().replace("-", "");
    }
}
