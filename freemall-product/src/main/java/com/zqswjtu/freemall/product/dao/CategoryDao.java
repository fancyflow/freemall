package com.zqswjtu.freemall.product.dao;

import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-25 21:40:14
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
