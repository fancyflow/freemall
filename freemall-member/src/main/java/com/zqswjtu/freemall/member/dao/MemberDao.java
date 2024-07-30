package com.zqswjtu.freemall.member.dao;

import com.zqswjtu.freemall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:57:39
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
