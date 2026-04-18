package com.kinetic.sports.security.common.adapter;

import cn.dev33.satoken.stp.StpInterface;
import com.kinetic.sports.bean.model.SysMenu;
import com.kinetic.sports.bean.model.SysRoleMenu;
import com.kinetic.sports.service.SysMenuService;
import com.kinetic.sports.service.SysRoleMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMenuService sysRoleMenuService;
    private final SysMenuService sysMenuService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
