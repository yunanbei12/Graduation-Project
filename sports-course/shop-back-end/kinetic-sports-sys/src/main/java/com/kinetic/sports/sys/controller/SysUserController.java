package com.kinetic.sports.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.SysUser;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserService.page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return ServerResponseEntity.success(result);
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<SysUser> detail(@PathVariable Long id) {
        SysUser sysUser = sysUserService.getById(id);
        sysUser.setPassword(null);
        return ServerResponseEntity.success(sysUser);
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody SysUser sysUser) {
        // 检查用户名唯一
        long count = sysUserService.count(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername())
        );
        if (count > 0) {
            return ServerResponseEntity.fail("用户名已存在");
        }
        sysUserService.save(sysUser);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody SysUser sysUser) {
        // 不允许修改密码通过此接口
        sysUser.setPassword(null);
        sysUserService.updateById(sysUser);
        return ServerResponseEntity.success();
    }

    @PutMapping("/password")
    public ServerResponseEntity<Void> resetPassword(@RequestBody SysUser sysUser) {
        SysUser dbUser = sysUserService.getById(sysUser.getId());
        if (dbUser == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        dbUser.setPassword(sysUser.getPassword());
        sysUserService.updateById(dbUser);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(id)) {
            return ServerResponseEntity.fail("不能删除当前登录用户");
        }
        sysUserService.removeById(id);
        return ServerResponseEntity.success();
    }

    @PutMapping("/status")
    public ServerResponseEntity<Void> updateStatus(@RequestBody SysUser sysUser) {
        SysUser dbUser = new SysUser();
        dbUser.setId(sysUser.getId());
        dbUser.setStatus(sysUser.getStatus());
        sysUserService.updateById(dbUser);
        return ServerResponseEntity.success();
    }
}
