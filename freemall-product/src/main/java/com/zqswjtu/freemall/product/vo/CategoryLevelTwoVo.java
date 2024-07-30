package com.zqswjtu.freemall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryLevelTwoVo {
    private String categoryLevelOneId;
    private List<CategoryLevelThreeVo> categoryLevelThreeList;
    private String id;
    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryLevelThreeVo {
        private String categoryLevelTwoId;
        private String id;
        private String name;
    }
}
