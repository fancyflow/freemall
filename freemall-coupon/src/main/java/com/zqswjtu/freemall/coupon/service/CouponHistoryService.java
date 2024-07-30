package com.zqswjtu.freemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.coupon.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:40:49
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

