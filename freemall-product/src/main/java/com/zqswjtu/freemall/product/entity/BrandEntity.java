package com.zqswjtu.freemall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.zqswjtu.common.validator.annotation.ListValue;
import com.zqswjtu.common.validator.group.AddGroup;
import com.zqswjtu.common.validator.group.UpdateGroup;
import com.zqswjtu.common.validator.group.UpdateShowStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author chaoching
 * @email swjtuqzhao@gmail.com
 * @date 2024-03-25 21:40:14
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定id", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(message = "logo地址不能为空", groups = {AddGroup.class})
	@URL(message = "logo地址必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(value = {0, 1}, message = "必须提交指定的值", groups = {AddGroup.class, UpdateShowStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	private Integer sort;

}
