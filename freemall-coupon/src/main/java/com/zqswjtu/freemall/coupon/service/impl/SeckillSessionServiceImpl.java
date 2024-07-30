package com.zqswjtu.freemall.coupon.service.impl;

import com.zqswjtu.freemall.coupon.entity.SeckillSkuRelationEntity;
import com.zqswjtu.freemall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.coupon.dao.SeckillSessionDao;
import com.zqswjtu.freemall.coupon.entity.SeckillSessionEntity;
import com.zqswjtu.freemall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatestThreeDaysSeckillSession() {
        // 1、计算最近三天时间需要上架的商品
        List<SeckillSessionEntity> list =
                this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
        if (list != null && !list.isEmpty()) {
            return list.stream().peek(seckillSessionEntity -> {
                Long id = seckillSessionEntity.getId();
                List<SeckillSkuRelationEntity> relationEntities =
                        seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                seckillSessionEntity.setRelationSkus(relationEntities);
            }).collect(Collectors.toList());
        }
        return null;
    }

    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getEndTime() {
        LocalDate time = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(time, max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}