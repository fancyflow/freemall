package com.zqswjtu.freemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.product.entity.AttrEntity;
import com.zqswjtu.freemall.product.vo.AttrGroupRelationVo;
import com.zqswjtu.common.vo.product.AttrResponseVo;
import com.zqswjtu.common.vo.product.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-25 21:40:14
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

