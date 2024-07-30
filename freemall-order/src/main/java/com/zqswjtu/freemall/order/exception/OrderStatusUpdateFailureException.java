package com.zqswjtu.freemall.order.exception;

public class OrderStatusUpdateFailureException extends RuntimeException {
    public OrderStatusUpdateFailureException(String msg) {
        super(msg);
    }
}
