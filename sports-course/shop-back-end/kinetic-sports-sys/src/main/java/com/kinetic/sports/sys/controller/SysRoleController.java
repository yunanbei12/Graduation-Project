package com.kinetic.sports.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.SysRole;
import com.kinetic.sports.bean.model.SysRoleMenu;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SysRoleMenuService;
import com.kinetic.sports.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleMenuService sysRoleMenuService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(SysRole::getName, name);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return ServerResponseEntity.success(sysRoleService.page(page, wrapper));
    }

    @GetMapping("/all")
    public ServerResponseEntity<List<SysRole>> all() {
        return ServerResponseEntity.success(sysRoleService.list(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1)
        ));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<SysRole> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(sysRoleService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody SysRole sysRole) {
        sysRoleService.updateById(sysRole);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        // 删除角色关联的菜单
        sysRoleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id)
        );
        return ServerResponseEntity.success();
    }

    @GetMapping("/menus/{roleId}")
    public ServerResponseEntity<List<Long>> roleMenus(@PathVariable Long roleId) {
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        return ServerResponseEntity.success(menuIds);
    }

    @PostMapping("/menus/{roleId}")
    public ServerResponseEntity<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        // 先删除原有的
        sysRoleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        // 再批量保存
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenus = menuIds.stream().map(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                return rm;
            }).collect(Collectors.toList());
            sysRoleMenuService.saveBatch(roleMenus);
        }
        return ServerResponseEntity.success();
    }
}
