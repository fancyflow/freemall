package com.zqswjtu.freemall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {
    /**
     * 会员收获地址列表，ums_member_receive_address
     **/
    @Getter
    @Setter
    List<MemberAddressVo> memberAddressVos;

    /**
     * 所有选中的购物项【购物车中的选中项】
     **/
    @Getter
    @Setter
    List<OrderItemVo> items;

    /**
     * 优惠券（会员积分）
     **/
    @Getter
    @Setter
    private Integer integration;

    /**
     * 防止重复提交令牌
     */
    @Getter
    @Setter
    private String uniqueToken;

    @Getter
    @Setter
    Map<Long, Boolean> stocks;

    public Integer getCount() {
        int count = 0;
        if (items != null) {
            for (OrderItemVo itemVo : items) {
                count += itemVo.getCount();
            }
        }
        return count;
    }

    // 计算订单总额
    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo itemVo : items) {
                BigDecimal multiply = itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    // 计算应付价格
    // TODO 减去顾客的优惠价格
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
