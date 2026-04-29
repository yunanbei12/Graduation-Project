package com.kinetic.sports.security.common.adapter;

import cn.dev33.satoken.stp.StpInterface;
import com.kinetic.sports.bean.model.SysMenu;
import com.kinetic.sports.bean.model.SysRoleMenu;
import com.kinetic.sports.bean.model.SysUser;
import com.kinetic.sports.service.SysMenuService;
import com.kinetic.sports.service.SysRoleMenuService;
import com.kinetic.sports.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMenuService sysRoleMenuService;
    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }
        if (Objects.equals(user.getRoleId(), 1L)) {
            return sysMenuService.list(new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 1))
                    .stream()
                    .map(SysMenu::getPermission)
                    .filter(permission -> permission != null && !permission.isBlank())
                    .distinct()
                    .collect(Collectors.toList());
        }
        List<Long> menuIds = sysRoleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, user.getRoleId()))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysMenuService.list(new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, 1))
                .stream()
                .map(SysMenu::getPermission)
                .filter(permission -> permission != null && !permission.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseLoginId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }
        return List.of(String.valueOf(user.getRoleId()));
    }

    private Long parseLoginId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
