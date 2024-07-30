package com.zqswjtu.freemall.order.exception;

public class OrderTimeOutException extends RuntimeException {
    public OrderTimeOutException(String msg) {
        super(msg);
    }
}
