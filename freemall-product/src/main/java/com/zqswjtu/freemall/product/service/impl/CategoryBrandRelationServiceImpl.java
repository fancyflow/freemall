package com.zqswjtu.freemall.product.service.impl;

import com.zqswjtu.freemall.product.dao.BrandDao;
import com.zqswjtu.freemall.product.dao.CategoryDao;
import com.zqswjtu.freemall.product.entity.BrandEntity;
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

import com.zqswjtu.freemall.product.dao.CategoryBrandRelationDao;
import com.zqswjtu.freemall.product.entity.CategoryBrandRelationEntity;
import com.zqswjtu.freemall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    BrandDao brandDao;
    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> list(Long brandId) {
        return baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        String brandName = brandDao.selectById(brandId).getName();
        String categoryName = categoryDao.selectById(catelogId).getName();
        categoryBrandRelation.setBrandName(brandName);
        categoryBrandRelation.setCatelogName(categoryName);
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String brandName) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandName(brandName);
        this.update(categoryBrandRelationEntity, new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String categoryName) {
        this.baseMapper.updateCategory(catId, categoryName);
    }

    @Override
    public List<BrandEntity> getBrandsByCatelogId(Long catId) {
        // 1、首先根据分类的id查询其关联的所有品牌id
        List<CategoryBrandRelationEntity> relationEntities = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        // 2、判断该分类是否关联了品牌
        // 这里先不判断看看会出什么问题
        List<Long> brandIds = relationEntities.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());
        // 如果不为空则查询该分类关联的所有品牌
        if (!brandIds.isEmpty()) {
            return this.brandDao.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
        }
        return new ArrayList<>();
    }

}