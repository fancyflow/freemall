package com.zqswjtu.freemall.product.feign;

import com.zqswjtu.common.to.SkuReductionTo;
import com.zqswjtu.common.to.SpuBoundTo;
import com.zqswjtu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("freemall-coupon")
public interface CouponFeignService {
    /**
     * 1、@RequestBody注解将SpuBoundTo对象转为json格式
     * 2、找到freemall-coupon服务对应的接口coupon/spubounds/save发送请求
     *    并且会将json格式的数据放在请求体位置
     * 3、对方服务收到请求，请求体内包含json数据，因为对方的接口也使用了@RequestBody
     *    会将收到的json格式数据转化成对应的实体类(根据字段名一一对应)
     * 所以只要json数据格式是兼容的，双方服务无需使用同一个TO对象
     * @param spuBoundTo
     * @return
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
