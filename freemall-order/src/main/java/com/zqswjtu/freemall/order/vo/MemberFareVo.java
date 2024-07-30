package com.zqswjtu.freemall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberFareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
