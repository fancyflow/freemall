package com.zqswjtu.freemall.authentication.util;

import java.util.Random;

public class RandomIntegerCodeUtils {
    private RandomIntegerCodeUtils() {}

    public static String getCode(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; ++i) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
