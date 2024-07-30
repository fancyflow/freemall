package com.zqswjtu.common.to.es;

import lombok.Data;

@Data
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;
}
