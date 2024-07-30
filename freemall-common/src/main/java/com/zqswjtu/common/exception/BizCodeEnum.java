package com.zqswjtu.common.exception;

/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *      003：短信验证码频率太高
 *  11: 商品
 *  12: 订单
 *  13: 购物车
 *  14: 物流
 *  15: 用户
 *  21: 库存
 *
 *
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(10002, "商品上架异常"),
    SMS_CODE_EXCEPTION(10003, "验证码获取频率太高，稍后再试"),
    USERNAME_EXIST_EXCEPTION(15001, "用户名已存在"),
    MOBILE_EXIST_EXCEPTION(15002, "手机号已注册"),
    LOGIN_EXCEPTION(15003, ""),
    NO_STOCK_EXCEPTION(21000, "商品库存不足");

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
