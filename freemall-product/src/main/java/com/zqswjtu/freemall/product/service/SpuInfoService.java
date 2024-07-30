package com.zqswjtu.freemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.product.entity.SkuInfoEntity;
import com.zqswjtu.freemall.product.entity.SpuInfoDescEntity;
import com.zqswjtu.freemall.product.entity.SpuInfoEntity;
import com.zqswjtu.freemall.product.vo.SpuVO;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-25 21:40:14
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuVO vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfo);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

