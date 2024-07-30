package com.zqswjtu.freemall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zqswjtu.common.exception.BizCodeEnum;
import com.zqswjtu.common.to.es.SkuHasStockTo;
import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.freemall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zqswjtu.freemall.ware.entity.WareSkuEntity;
import com.zqswjtu.freemall.ware.service.WareSkuService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;



/**
 * 商品库存
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:25:16
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/order/lockStock")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        Boolean isLocked = false;
        try {
            isLocked = wareSkuService.orderLockStock(vo);
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
        if (isLocked) {
            return R.ok();
        }
        return R.error();
    }

    // 查询sku是否有库存
    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockTo> vos = wareSkuService.getSkusHasStock(skuIds);
        return R.ok().put("data", vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.saveWareSku(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
