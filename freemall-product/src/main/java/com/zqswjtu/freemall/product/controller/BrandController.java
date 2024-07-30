package com.zqswjtu.freemall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.zqswjtu.common.validator.group.AddGroup;
import com.zqswjtu.common.validator.group.UpdateGroup;
import com.zqswjtu.common.validator.group.UpdateShowStatusGroup;
import com.zqswjtu.freemall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zqswjtu.freemall.product.entity.BrandEntity;
import com.zqswjtu.freemall.product.service.BrandService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 19:42:57
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     * @RequestParam用来处理 Content-Type 为 application/x-www-form-urlencoded 编码的内容，
     * Content-Type默认为该属性。
     * GET请求中，因为没有HttpEntity，所以@RequestBody并不适用。
     * 并且@RequestParam不支持批量插入数据
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 注解@RequestBody接收的参数是来自requestBody中，即请求体
     * 一般用于处理非 Content-Type: application/x-www-form-urlencoded编码格式的数据，
     * 比如：application/json、application/xml等类型的数据。
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // 统一的异常处理方式，参数后面没有追加BindingResult会跳转到统一的处理方法上
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand) {
        brandService.save(brand);
        return R.ok();
    }
    // 普通校验参数的方式
    /*
    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            // 1、获取校验的错误结果
            result.getFieldErrors().forEach(item -> {
                // 获取错误提示信息
                String message = item.getDefaultMessage();
                // 获取校验不合法的字段名
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(400, "提交的数据不合法").put("data", map);
        } else {
            brandService.save(brand);
            return R.ok();
        }
    }
    */

    /**
     * 修改
     */
    @Transactional
    @RequestMapping("/update")
    // @RequiresPermissions("product:brand:update")
    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateById(brand);
        // 更新数据库表中的冗余字段
        if (!StringUtils.isEmpty(brand.getName())) {
            // 同步更新其它表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }
        return R.ok();
    }

    // 仅仅修改showStatus
    @RequestMapping("/update/status")
    public R updateShowStatus(@Validated(value = {UpdateShowStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
