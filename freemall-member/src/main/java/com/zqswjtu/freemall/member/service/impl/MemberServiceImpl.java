package com.zqswjtu.freemall.member.service.impl;

import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.member.dao.MemberLevelDao;
import com.zqswjtu.freemall.member.entity.MemberLevelEntity;
import com.zqswjtu.freemall.member.vo.LoginVo;
import com.zqswjtu.freemall.member.vo.RegisterVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.member.dao.MemberDao;
import com.zqswjtu.freemall.member.entity.MemberEntity;
import com.zqswjtu.freemall.member.service.MemberService;

import javax.security.auth.login.LoginException;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(RegisterVo registerVo) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认会员等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        // 设置账号属性
        // TODO 这里需要检查用户名的唯一性
        memberEntity.setUsername(registerVo.getUsername());
        // TODO 这里需要检查改手机号是否被注册过
        memberEntity.setMobile(registerVo.getMobile());
        // 密码加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registerVo.getPassword());
        memberEntity.setPassword(encode);

        // 其它默认属性

        // 保存
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public MemberResponseVo login(LoginVo loginVo) throws LoginException {
        String account = loginVo.getAccount();
        String password = loginVo.getPassword();

        // TODO 如何避免某个人的username和别人的电话号码相同
        //  解决方案 使用正则表达式限制username不能以数字开头
        List<MemberEntity> memberEntities = this.baseMapper.selectList(
                new QueryWrapper<MemberEntity>().eq("username", account)
                        .or().eq("mobile", account)
        );
        if (memberEntities.isEmpty()) {
            // 用户名不存在
            throw new LoginException("用户名不存在");
        } else {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            for (MemberEntity entity : memberEntities) {
                String encodedPassword = entity.getPassword();
                if (passwordEncoder.matches(password, encodedPassword)) {
                    MemberResponseVo memberResponseVo = new MemberResponseVo();
                    BeanUtils.copyProperties(entity, memberResponseVo);
                    return memberResponseVo;
                }
            }
        }
        throw new LoginException("用户名或密码错误");
    }
}
