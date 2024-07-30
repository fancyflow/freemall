package com.zqswjtu.freemall.product.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zqswjtu.common.to.SkuNameTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zqswjtu.freemall.product.entity.SkuInfoEntity;
import com.zqswjtu.freemall.product.service.SkuInfoService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;



/**
 * sku信息
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 19:42:57
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}/price")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getById(skuId).getPrice();
    }

    @PostMapping("/getSkuName")
    public R getSkuName(@RequestBody SkuNameTo skuNameTo) {
        String skuName = skuInfoService.getById(skuNameTo.getSkuId()).getSkuName();
        return R.ok().put("data", skuName);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    // @RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId) {
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
