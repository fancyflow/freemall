package com.zqswjtu.freemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.to.es.SkuHasStockTo;
import com.zqswjtu.common.to.mq.OrderTo;
import com.zqswjtu.common.to.mq.StockLockedTo;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.ware.entity.WareSkuEntity;
import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.freemall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:25:16
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);

    void saveWareSku(WareSkuEntity wareSku);

    Boolean orderLockStock(WareSkuLockVo vo) throws NoStockException;

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo order);
}
