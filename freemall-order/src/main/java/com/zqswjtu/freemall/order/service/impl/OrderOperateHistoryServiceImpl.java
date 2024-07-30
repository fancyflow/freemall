package com.zqswjtu.freemall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.order.dao.OrderOperateHistoryDao;
import com.zqswjtu.freemall.order.entity.OrderOperateHistoryEntity;
import com.zqswjtu.freemall.order.service.OrderOperateHistoryService;


@Service("orderOperateHistoryService")
public class OrderOperateHistoryServiceImpl extends ServiceImpl<OrderOperateHistoryDao, OrderOperateHistoryEntity> implements OrderOperateHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderOperateHistoryEntity> page = this.page(
                new Query<OrderOperateHistoryEntity>().getPage(params),
                new QueryWrapper<OrderOperateHistoryEntity>()
        );

        return new PageUtils(page);
    }

}