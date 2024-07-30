package com.zqswjtu.freemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.zqswjtu.freemall.product.vo.CategoryLevelTwoVo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-25 21:40:14
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> list);

    Long[] getCatelogPathById(Long attrGroupId);

    List<CategoryEntity> getLevelOneCategories();

    Map<String, List<CategoryLevelTwoVo>> getCategoryJson();
}

