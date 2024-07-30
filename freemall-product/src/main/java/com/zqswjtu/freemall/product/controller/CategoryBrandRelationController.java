package com.zqswjtu.freemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zqswjtu.freemall.product.entity.BrandEntity;
import com.zqswjtu.freemall.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zqswjtu.freemall.product.entity.CategoryBrandRelationEntity;
import com.zqswjtu.freemall.product.service.CategoryBrandRelationService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 19:42:57
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/catelog/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R catelogList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(brandId);

        return R.ok().put("data", data);
    }

    /**
     * 获取分类关联的所有品牌
     * 1、Controller接收到请求后先接收和校验数据
     * 2、完成数据校验后调用Service层传来的数据进行业务处理
     * 3、Controller接受Service层处理完的数据，封装成页面指定的vo
     * @return
     */
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId") Long catId) {
        List<BrandEntity> list = categoryBrandRelationService.getBrandsByCatelogId(catId);
        List<BrandVo> brandVos = list.stream().map(item -> {
            BrandVo vo = new BrandVo();
            vo.setBrandId(item.getBrandId());
            vo.setBrandName(item.getName());
            return vo;
        }).collect(Collectors.toList());
        return R.ok().put("data", brandVos);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
