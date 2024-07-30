package com.zqswjtu.freemall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zqswjtu.freemall.product.dao.AttrGroupDao;
import com.zqswjtu.freemall.product.dao.SkuSaleAttrValueDao;
import com.zqswjtu.freemall.product.entity.BrandEntity;
import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.zqswjtu.freemall.product.service.BrandService;
import com.zqswjtu.freemall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class FreemallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    /**
     * 在Spring Boot的开发中循环依赖是一个常见的问题，两个或多个类之间存在彼此依赖的情况，
     * 形成一个循环依赖链，在2.6.0之前，Spring Boot会自动处理循环依赖的问题，而2.6.0版
     * 本以上开始检查循环依赖，存在该问题则会报错。
     * @Service
     *  class A {
     *
     *     @Resource
     *     private B b;
     *
     * }
     *
     * @Service
     *  class B {
     *
     *     @Resource
     *     private A a;
     *
     * }
     */

    @Test
    void test() {
        CategoryEntity entity = categoryService.getBaseMapper().selectById(225);
        System.out.println(entity);
//        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
//        ops.set("Hello", "World" + UUID.randomUUID());
        System.out.println(redissonClient);
        System.out.println(attrGroupDao.getAttrGroupWithAttrsBySpuId(7L, 225L));
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(7L));
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功！");

//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");
//        brandService.updateById(brandEntity);
//        System.out.println("修改成功！");
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(System.out::println);
    }
}
