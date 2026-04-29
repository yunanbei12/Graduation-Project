package com.kinetic.sports.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.SysMenu;
import com.kinetic.sports.bean.model.SysRoleMenu;
import com.kinetic.sports.bean.model.SysUser;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SysMenuService;
import com.kinetic.sports.service.SysRoleMenuService;
import com.kinetic.sports.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;
    private final SysRoleMenuService sysRoleMenuService;

    @GetMapping("/list")
    public ServerResponseEntity<List<SysMenu>> list() {
        List<SysMenu> menus = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort)
        );
        return ServerResponseEntity.success(menus);
    }

    @GetMapping("/tree")
    public ServerResponseEntity<List<SysMenu>> tree() {
        List<SysMenu> allMenus = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .orderByAsc(SysMenu::getSort)
        );
        List<SysMenu> tree = buildTree(allMenus, 0L);
        return ServerResponseEntity.success(tree);
    }

    /**
     * 获取当前登录用户的菜单（根据角色权限过滤）
     */
    @GetMapping("/user")
    public ServerResponseEntity<List<SysMenu>> userMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        
        // 超级管理员（roleId=1）返回所有菜单
        if (user.getRoleId() != null && user.getRoleId() == 1L) {
            List<SysMenu> allMenus = sysMenuService.list(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 1)
                            .orderByAsc(SysMenu::getSort)
            );
            return ServerResponseEntity.success(buildTreeWithChildren(allMenus, 0L));
        }
        
        // 根据角色获取菜单ID列表
        if (user.getRoleId() == null) {
            return ServerResponseEntity.success(new ArrayList<>());
        }
        
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, user.getRoleId())
        );
        
        if (roleMenus.isEmpty()) {
            return ServerResponseEntity.success(new ArrayList<>());
        }
        
        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
        
        // 获取菜单详情
        List<SysMenu> menus = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, 1)
                        .orderByAsc(SysMenu::getSort)
        );
        
        return ServerResponseEntity.success(buildTreeWithChildren(menus, 0L));
    }

    /**
     * 构建树形结构，并设置children属性
     */
    private List<SysMenu> buildTreeWithChildren(List<SysMenu> allMenus, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                List<SysMenu> children = buildTreeWithChildren(allMenus, menu.getId());
                menu.setChildren(children.isEmpty() ? null : children);
                result.add(menu);
            }
        }
        return result;
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<SysMenu> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(sysMenuService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateById(sysMenu);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        // 检查是否有子菜单
        long childCount = sysMenuService.count(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)
        );
        if (childCount > 0) {
            return ServerResponseEntity.fail("存在子菜单，无法删除");
        }
        sysMenuService.removeById(id);
        return ServerResponseEntity.success();
    }

    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> {
                    List<SysMenu> children = buildTree(allMenus, m.getId());
                    // 不在entity中添加children字段，前端自行构建树
                })
                .collect(Collectors.toList());
    }
}
