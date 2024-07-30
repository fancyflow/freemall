package com.zqswjtu.freemall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:09:26
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

