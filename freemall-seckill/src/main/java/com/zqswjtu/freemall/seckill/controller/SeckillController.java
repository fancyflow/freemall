package com.zqswjtu.freemall.seckill.controller;

import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.seckill.service.SeckillService;
import com.zqswjtu.freemall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    private SeckillService seckillService;
    /**
     * 返回当前时间可以参与秒杀活动的商品
     * @return
     */
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> list = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(list);
    }

    @GetMapping("/getSkuSeckillInfo")
    @ResponseBody
    public R getSkuSeckillInfo(@RequestParam("skuId") Long skuId) {
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfoBySkuId(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        // 1、通过拦截器判断是否登录
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
