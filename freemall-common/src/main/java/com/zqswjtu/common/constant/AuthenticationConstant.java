package com.zqswjtu.common.constant;

import java.util.concurrent.TimeUnit;

public class AuthenticationConstant {
    public static final int SMS_CODE_LENGTH = 6;
    public static final int SMS_CODE_EXPIRATION_TIME = 5 * 60;
    public static final TimeUnit SMS_CODE_EXPIRATION_TIME_UNIT = TimeUnit.SECONDS;
    public static final String SMS_CODE_CACHE_PREFIX = "sms:code:";
}
