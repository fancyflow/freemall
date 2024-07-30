package com.zqswjtu.freemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.freemall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 21:25:16
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

