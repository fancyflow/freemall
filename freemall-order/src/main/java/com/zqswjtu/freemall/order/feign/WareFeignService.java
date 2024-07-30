package com.zqswjtu.freemall.order.feign;

import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("freemall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/order/lockStock")
    R orderLockStock(@RequestBody WareSkuLockVo vo);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);
}
