package com.zqswjtu.common.constant;

import lombok.Getter;

public class ProductConstant {
    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private int code;
        private String msg;
        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }

    @Getter
    public enum StatusEnum {
        NEW_SPU(0, "新建"),
        UP_SPU(1, "上架"),
        DOWN_SPU(2, "下架");

        private int code;
        private String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
