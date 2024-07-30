package com.zqswjtu.freemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.member.entity.MemberEntity;
import com.zqswjtu.freemall.member.vo.LoginVo;
import com.zqswjtu.freemall.member.vo.RegisterVo;

import javax.security.auth.login.LoginException;
import java.util.Map;

/**
 * 会员
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:57:39
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(RegisterVo registerVo);

    MemberResponseVo login(LoginVo loginVo) throws LoginException;
}
