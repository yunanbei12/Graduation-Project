package com.kinetic.sports.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    /** 0=目录 1=菜单 2=按钮 */
    private Integer type;

    private String permission;

    private Integer status;

    /** 子菜单（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
