package com.zqswjtu.freemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.ware.entity.WareInfoEntity;
import com.zqswjtu.freemall.ware.vo.MemberFareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:25:16
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberFareVo getFare(Long addrId);
}

