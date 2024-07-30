package com.zqswjtu.freemall.product.web;

import com.zqswjtu.freemall.product.service.SkuInfoService;
import com.zqswjtu.freemall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVo skuItem = skuInfoService.getItemBySkuId(skuId);
//        System.out.println(skuItem);
        model.addAttribute("item", skuItem);
        return "item";
    }
}
