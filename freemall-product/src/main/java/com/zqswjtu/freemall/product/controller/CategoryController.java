package com.zqswjtu.freemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zqswjtu.freemall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.zqswjtu.freemall.product.service.CategoryService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 19:42:57
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表，显示产品分类，以树形结构显示出来
     */
    @RequestMapping("/list/tree")
    public R list(){
        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
        // TODO 判断新添加的菜单是否已经存在
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @Transactional
    @RequestMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
        // TODO 判断修改后的菜单是否已经存在
		categoryService.updateById(category);
        // 更新数据库表中的冗余字段
        if (!StringUtils.isEmpty(category.getName())) {
            // 同步更新其它表中的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
        return R.ok();
    }

    /**
     * 删除
     * @RequestBody: 获取请求体，必须发送POST请求
     * SpringMVC自动将请求体的JSON数据转为对应的对象
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        // 1、检查当前删除的菜单是否被别的地方引用
		categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
