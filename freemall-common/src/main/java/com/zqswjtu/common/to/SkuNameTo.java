package com.zqswjtu.common.to;

import lombok.Data;

@Data
public class SkuNameTo {
    private Long skuId;
    private Long wareId;
    private Integer stock;
    private String skuName;
    private Integer stockLocked;
}
