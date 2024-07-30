package com.zqswjtu.freemall.product.service.impl;

import com.zqswjtu.freemall.product.entity.AttrEntity;
import com.zqswjtu.freemall.product.service.AttrService;
import com.zqswjtu.freemall.product.vo.AttrGroupWithAttrsVo;
import com.zqswjtu.freemall.product.vo.SkuItemVo;
import com.zqswjtu.freemall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.product.dao.AttrGroupDao;
import com.zqswjtu.freemall.product.entity.AttrGroupEntity;
import com.zqswjtu.freemall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page;
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> obj.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        // catelogId为0则查询全部数据
        if (catelogId == 0) {
            page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
        wrapper.eq("catelog_id", catelogId);
        page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * 根据分类id查出所有的分组以及这些分组对应的取值属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2、查询分组所有的属性
        if (!attrGroupEntities.isEmpty()) {
            List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
                AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(item, vo);
                List<AttrEntity> attr = attrService.getRelationAttr(vo.getAttrGroupId());
                vo.setAttrs(attr);
                return vo;
            }).collect(Collectors.toList());
            return collect;
        }
        return new ArrayList<>();
    }

    /**
     * 查询当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
     * @param spuId
     * @param catelogId
     * @return
     */
    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catelogId) {
        // 1.通过spuId查询所有属性值（pms_product_attr_value）
        // 2.通过attrId关联所有属性分组（pms_attr_attrgroup_relation）
        // 3.通过attrGroupId + catalogId关联属性分组名称（pms_attr_group）
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catelogId);
    }

}