package com.zqswjtu.freemall.search.vo;

import com.zqswjtu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResultVo {
    private List<SkuEsModel> products;

    private Integer pageNum;
    private Long total;
    private Long totalPages;
    private List<Integer> pageNavs;

    private List<BrandVo> brands;
    private List<CatelogVo> catelogs;
    private List<AttrVo> attrs = new ArrayList<>();

    // ============================以上是要返回的数据====================================

    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();

    // 封装筛选条件中的属性id集合【用于面包屑，选择属性后出现在面包屑中，下面的属性栏则隐藏】
    // 该字段是提供前端用的
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class BrandVo {
        private Integer brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatelogVo {
        private Long catelogId;
        private String catelogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }


    /**
     * 面包屑导航VO
     */
    @Data
    public static class NavVo {
        private String navName;// 属性名
        private String navValue;// 属性值
        private String link;// 回退地址（删除该面包屑筛选条件回退地址）
    }
}
