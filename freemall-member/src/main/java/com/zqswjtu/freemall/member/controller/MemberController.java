package com.zqswjtu.freemall.member.controller;

import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zqswjtu.common.exception.BizCodeEnum;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.member.feign.CouponFeignService;
import com.zqswjtu.freemall.member.vo.LoginVo;
import com.zqswjtu.freemall.member.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zqswjtu.freemall.member.entity.MemberEntity;
import com.zqswjtu.freemall.member.service.MemberService;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.R;

import javax.security.auth.login.LoginException;


/**
 * 会员
 *
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-26 20:57:39
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;
    /**
     * 调用freemall-coupon模块的远程服务
     */
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("gerald");
        R memberCoupons = couponFeignService.getMemberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) {
        MemberResponseVo memberEntity = null;
        try {
             memberEntity = memberService.login(loginVo);
        } catch (LoginException e) {
            return R.error(BizCodeEnum.LOGIN_EXCEPTION.getCode(), e.getMessage());
        }
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        }
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }

    @PostMapping("/register")
    public R register(@RequestBody RegisterVo registerVo) {
        memberService.register(registerVo);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
