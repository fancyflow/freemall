package com.zqswjtu.common.exception;

public class NoStockException extends RuntimeException {
    private Long skuId;
    private String msg;

    public NoStockException(String msg) {
        super(msg);
    }

    public NoStockException(Long skuId) {
        super("商品id：" + skuId + "没有库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
