package com.zqswjtu.freemall.product.service.impl;

import com.zqswjtu.common.constant.ProductConstant;
import com.zqswjtu.common.to.SkuReductionTo;
import com.zqswjtu.common.to.SpuBoundTo;
import com.zqswjtu.common.to.es.SkuEsModel;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.product.entity.*;
import com.zqswjtu.freemall.product.feign.CouponFeignService;
import com.zqswjtu.freemall.product.feign.SearchFeignService;
import com.zqswjtu.freemall.product.feign.WareFeignService;
import com.zqswjtu.freemall.product.service.*;
import com.zqswjtu.freemall.product.vo.SpuVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * seata AT分布式事务保证各个远程调用之间的数据一致性
     * 使用@GlobalTransactional注解实现, 适合非高并发的场景
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuVO vo) {
        // 1、保存spu基本信息
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfo);
        spuInfo.setCreateTime(new Date());
        spuInfo.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfo);

        // 2、保存spu的描述图片
        List<String> descript = vo.getDescript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfo.getId());
        spuInfoDescEntity.setDecript(String.join(",", descript));
        spuInfoDescService.saveSpuInfoDescript(spuInfoDescEntity);

        // 3、保存spu的图片集
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfo.getId(), images);

        // 4、保存spu的规格参数
        List<SpuVO.BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfo.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        // 5、保存spu的积分信息
        SpuVO.Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfo.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 5、保存当前spu对象的所有sku信息
        List<SpuVO.Sku> skus = vo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(item -> {
                // ①sku基本信息
                String defaultImage = "";
                for (SpuVO.Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfo.getBrandId());
                skuInfoEntity.setCatelogId(spuInfo.getCatelogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfo.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                // ②sku图片信息
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages()
                        .stream()
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        })
                        .filter(entity -> !StringUtils.isEmpty(entity.getImgUrl()))
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);
                // TODO 没有图片路径的无需保存

                // ③sku销售属性信息
                List<SpuVO.Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // ④sku优惠满减等信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                // skuReductionTo.setMemberPrice(item.getMemberPrice());
                List<SpuVO.MemberPrice> memberPrice = item.getMemberPrice();
                List<SkuReductionTo.MemberPrice> list = new ArrayList<>();
                for (SpuVO.MemberPrice price : memberPrice) {
                    SkuReductionTo.MemberPrice memberPrice1 = new SkuReductionTo.MemberPrice();
                    memberPrice1.setId(price.getId());
                    memberPrice1.setName(price.getName());
                    memberPrice1.setPrice(price.getPrice());
                    list.add(memberPrice1);
                }
                skuReductionTo.setMemberPrice(list);
                if (skuReductionTo.getFullCount() > 0 ||
                        skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfo) {
        this.baseMapper.insert(spuInfo);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
               w.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1、组装需要的数据
        // ①查询当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkuInfoBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // TODO 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 根据attrId到pms_attr表中查询search_type为1的attr
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> searchAttrIdsSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrs = baseAttrs.stream()
                .filter(attr -> searchAttrIdsSet.contains(attr.getAttrId()))
                .map(attr -> {
                    SkuEsModel.Attrs newAttr = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(attr, newAttr);
                    return newAttr;
                })
                .collect(Collectors.toList());

        Map<Long, Boolean> stockMap = new HashMap<>();
        try {
            R hasStock = wareFeignService.getSkusHasStock(skuIds);
            List<Map<String, Object>> data = (List<Map<String, Object>>) hasStock.get("data");
            for (Map<String, Object> stockTo : data) {
                stockMap.put(((Integer) stockTo.get("skuId")).longValue(), (Boolean) stockTo.get("hasStock"));
            }
        } catch (Exception e) {
            log.error("库存查询服务异常：原因{}", e);
        }

        // ②封装每个sku的信息
        List<SkuEsModel> collect = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, esModel);
            // skuPrice
            esModel.setSkuPrice(skuInfoEntity.getPrice());
            // skuImg
            esModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            // hasStock
            // TODO 发送远程调用查询是否有库存
            Boolean hasStock = stockMap.get(skuInfoEntity.getSkuId());
            esModel.setHasStock(hasStock != null && hasStock);

            // hotScore
            // TODO 热度评分
            esModel.setHotScore(0L);

            // brandName brandImg catelogName
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            esModel.setCatelogName(categoryService.getById(esModel.getCatelogId()).getName());

            // attrs 设置检索属性
            esModel.setAttrs(attrs);
            return esModel;
        }).collect(Collectors.toList());

        // TODO 将数据发送给freemall-search进行保存
        R r = searchFeignService.productStatusUp(collect);
        if (r.getCode() == 0) {
            // 远程调用成功
            // TODO 修改当前spu状态为上架
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        } else {
            // 远程调用失败
            // TODO 接口幂等性：重试机制
        }
    }

    /**
     * 根据商品的skuId查询对应的spu信息
     * @param skuId
     * @return
     */
    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity infoEntity = skuInfoService.getById(skuId);
        Long spuId = infoEntity.getSpuId();
        return this.getById(spuId);
    }
}