package com.zqswjtu.freemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.ware.feign.MemberFeignService;
import com.zqswjtu.freemall.ware.vo.MemberAddressVo;
import com.zqswjtu.freemall.ware.vo.MemberFareVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.ware.dao.WareInfoDao;
import com.zqswjtu.freemall.ware.entity.WareInfoEntity;
import com.zqswjtu.freemall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     * @param addrId
     * @return
     */
    @Override
    public MemberFareVo getFare(Long addrId) {
        MemberFareVo memberFareVo = new MemberFareVo();
        R info = memberFeignService.info(addrId);
        if (info.getCode() != 0) {
            // 调用失败
            return null;
        }
        MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if (data != null) {
            memberFareVo.setAddress(data);
            String phone = data.getPhone();
            memberFareVo.setFare(new BigDecimal(phone.substring(phone.length() - 1)));
            return memberFareVo;
        }
        return null;
    }

}