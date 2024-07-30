package com.zqswjtu.common.enums;

public enum OrderStatusEnum {
    CREATE_NEW(0, "待付款"),
    PAYED(1, "已付款"),
    SENT(2, "已发货"),
    RECEIVED(3, "已收货"),
    CANCELED(4, "已取消"),
    SERVICING(5, "售后中"),
    SERVICED(6, "售后完成")
    ;

    private String msg;
    private Integer code;

    OrderStatusEnum(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
