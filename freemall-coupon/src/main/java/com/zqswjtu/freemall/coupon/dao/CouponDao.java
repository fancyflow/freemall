package com.zqswjtu.freemall.coupon.dao;

import com.zqswjtu.freemall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:40:49
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
