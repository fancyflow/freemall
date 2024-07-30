package com.zqswjtu.freemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.common.enums.OrderStatusEnum;
import com.zqswjtu.common.to.SkuNameTo;
import com.zqswjtu.common.to.es.SkuHasStockTo;
import com.zqswjtu.common.to.mq.OrderTo;
import com.zqswjtu.common.to.mq.StockDetailTo;
import com.zqswjtu.common.to.mq.StockLockedTo;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.freemall.ware.entity.WareOrderTaskDetailEntity;
import com.zqswjtu.freemall.ware.entity.WareOrderTaskEntity;
import com.zqswjtu.freemall.ware.feign.OrderFeignService;
import com.zqswjtu.freemall.ware.feign.ProductFeignService;
import com.zqswjtu.freemall.ware.service.WareOrderTaskDetailService;
import com.zqswjtu.freemall.ware.service.WareOrderTaskService;
import com.zqswjtu.freemall.ware.vo.OrderItemVo;
import com.zqswjtu.freemall.ware.vo.OrderVo;
import com.zqswjtu.freemall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.ware.dao.WareSkuDao;
import com.zqswjtu.freemall.ware.entity.WareSkuEntity;
import com.zqswjtu.freemall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            to.setSkuId(skuId);
            // 查询当前sku的总库存量
            Long stock = baseMapper.getSkuStockById(skuId);
            to.setHasStock(stock != null && stock > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void saveWareSku(WareSkuEntity wareSku) {
        SkuNameTo skuNameTo = new SkuNameTo();
        skuNameTo.setSkuId(wareSku.getSkuId());
        String skuName = null;
        try {
            R r = productFeignService.getSkuName(skuNameTo);
            skuName = (String) r.get("data");
        } catch (Exception e) {
            log.error("远程调用查询skuName失败，原因：{}", e);
        }
        wareSku.setSkuName(skuName);
        this.save(wareSku);
    }

    /**
     * 为某个订单锁定库存
     * 默认出现运行时异常时就会回滚操作
     * 库存解锁场景
     * 1、下订单成功，库存锁定成功，订单过期没有支付被系统自动取消、被用户手动取消；
     * 2、下订单成功，库存锁定成功，接下来的业务出现异常导致订单回滚；
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) throws NoStockException {
        /**
         * 保存和这个订单对应的库存工作单详情，便于后续解锁库存
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);
        Long wareOrderTaskId = wareOrderTaskEntity.getId();

        // TODO 按照下单的地址，找到一个最近的仓库锁定库存，暂时没有实现

        // TODO 如何保证查询库存和修改库存的原子性，暂时没有实现
        // 一个事务执行的任何过程中都可以获得锁，但是只有事务提交或回滚的时候才释放这些锁。
        // 可以使用select ... for update单独为每个商品锁定库存，但是这样很大可能会频繁死锁，暂时没有想到好的解决方法

        List<OrderItemVo> lockItems = vo.getLockItems();
        // 查询哪些仓库有库存
        List<SkuHasStock> collect = lockItems.stream().map(item -> {
            SkuHasStock stock = new SkuHasStock();
            stock.setNum(item.getCount());
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 尝试锁定库存
        for (SkuHasStock hasStock : collect) {
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if (wareIds == null || wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }
            boolean flag = false;
            for (Long wareId : wareIds) {
                // 成功就返回1，否则返回0
                int count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    flag = true;
                    // 订单中某件商品库存锁定成功后保存库存锁定状态信息，便于后续解锁库存
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null,
                            skuId, null, hasStock.getNum(), wareOrderTaskId, wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskId);
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetailTo(stockDetailTo);
                    // 给消息队列发送消息，库存锁定成功
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            // 如果某个商品锁定库存失败，事务就会回滚，之前锁定的库存操作全部会回滚
            // 已经发送到消息队列的成功锁定库存的商品，其库存工作单详情会在查询数据库时而查不到结果，所以就不用解锁
            if (!flag) {
                throw new NoStockException(skuId);
            }
        }
        // 库存锁定成功
        return true;
    }

    @Transactional
    @Override
    public void unLockStock(StockLockedTo to) {
        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();
        /**
         * 根据数据库是否存在库存工作单来判断是否需要解锁库存
         * 1、如果没有则无需解锁，因为这种情况肯定是坤村锁定失败了
         * 2、如果有则需要进一步进行判断是否需要解锁库存
         */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 需要解锁
            Long id = to.getId(); // 库存工作单的id
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(id);
            String orderSn = wareOrderTaskEntity.getOrderSn(); // 根据订单号远程查询该订单对应的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                // 订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>(){});
                // data为null，说明库存锁定成功，但是订单业务回滚了，此时也要解锁库存
                if (data == null || OrderStatusEnum.CANCELED.getCode().equals(data.getStatus())) {
                    // 订单已经被取消才能解锁库存
                    if (byId.getLockStatus().equals(1)) {
                        // 只有库存工作单的状态为锁定状态才能解锁
                        // 真正解锁库存
                        doUnLockStock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    }
                }
            } else {
                // 消息拒绝以后重新入队，让别人正确解锁库存
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁, 直接发送ack给消息队列
        }
    }

    // 防止订单服务卡顿，导致订单消息一直改不了，库存解锁优先到期，查询到订单为新建状态，什么都不做就走了
    // 导致卡顿的订单永远不能解锁
    @Transactional
    @Override
    public void unLockStock(OrderTo order) {
        WareOrderTaskEntity wareOrderTaskEntity =
                wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", order.getOrderSn()));
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.getBaseMapper()
                .selectList(
                        new QueryWrapper<WareOrderTaskDetailEntity>()
                                .eq("task_id", wareOrderTaskEntity.getId())
                                .eq("lock_status", 1)
                );
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            doUnLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(), detailEntity.getId());
        }
    }

    private void doUnLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unLockStock(skuId, wareId, num);
        // 将订单中每项商品对应的库存工作单中的lock_status修改为已解锁状态：2
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2); // 变为已解锁状态
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Data
    private static class SkuHasStock {
        // 商品id
        private Long skuId;
        private Integer num;
        // 仓库id
        private List<Long> wareIds;
    }
}