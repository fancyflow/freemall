package com.zqswjtu.common.vo.product;

import com.zqswjtu.common.vo.product.AttrVo;
import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo {
    // 所属分类名字，例如：手机/手机通讯/手机
    private String catelogName;
    // 所属分组名字，例如：主体、基本信息
    private String groupName;
    // 品牌目录
    private Long[] catelogPath;
}
