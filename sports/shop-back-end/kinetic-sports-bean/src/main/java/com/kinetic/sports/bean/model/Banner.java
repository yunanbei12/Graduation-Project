package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("banner")
public class Banner extends BaseEntity {

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 跳转类型 1=页面 2=小程序 3=H5 */
    private Integer linkType;

    /** 排序(数字越小越靠前) */
    private Integer sort;

    /** 位置 1=首页 2=课程页 */
    private Integer position;

    /** 0=禁用 1=启用 */
    private Integer status;
}
