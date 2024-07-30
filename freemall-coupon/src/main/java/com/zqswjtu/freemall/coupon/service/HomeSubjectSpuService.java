package com.zqswjtu.freemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * 专题商品
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:40:49
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

