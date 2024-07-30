package com.zqswjtu.freemall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.common.to.es.SkuEsModel;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.product.entity.SkuImagesEntity;
import com.zqswjtu.freemall.product.entity.SpuInfoDescEntity;
import com.zqswjtu.freemall.product.feign.SeckillFeignService;
import com.zqswjtu.freemall.product.service.*;
import com.zqswjtu.freemall.product.vo.SeckillInfoVo;
import com.zqswjtu.freemall.product.vo.SkuItemSaleAttrVo;
import com.zqswjtu.freemall.product.vo.SkuItemVo;
import com.zqswjtu.freemall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.product.dao.SkuInfoDao;
import com.zqswjtu.freemall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
               w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal(0)) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuInfoBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo getItemBySkuId(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        // 异步方式完成下列任务
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息的获取
            SkuInfoEntity infoEntity = this.getById(skuId);
            skuItemVo.setInfo(infoEntity);
            return infoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            // 3、获取spu销售属性的组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((result) -> {
            // 4、获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(result.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            // 5、获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos =
                    attrGroupService.getAttrGroupWithAttrsBySpuId(result.getSpuId(),
                            result.getCatelogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2、sku的图片信息
            List<SkuImagesEntity> images = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(images);
        }, executor);

        // 查询当前sku商品是否参与秒杀
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = r.getData(new TypeReference<SeckillInfoVo>(){});
                skuItemVo.setSeckillSku(seckillInfoVo);
            }
        }, executor);

        // 等待上述所有future任务都完成才返回查询结果
        try {
            CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, seckillFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*
        // 1、sku基本信息的获取
        SkuInfoEntity infoEntity = this.getById(skuId);
        skuItemVo.setInfo(infoEntity);
        Long catelogId = infoEntity.getCatelogId();
        Long spuId = infoEntity.getSpuId();

        // 2、sku的图片信息
        List<SkuImagesEntity> images = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
        skuItemVo.setImages(images);

        // 3、获取spu销售属性的组合
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(saleAttrVos);

        // 4、获取spu的介绍
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(spuInfoDescEntity);

        // 5、获取spu的规格参数信息
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catelogId);
        skuItemVo.setGroupAttrs(attrGroupVos);
        */

        return skuItemVo;
    }

}