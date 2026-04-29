package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.User;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;

    /**
     * 小程序用户分页列表
     */
    @GetMapping("/list")
    public ServerResponseEntity<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getNickName, keyword)
                    .or().like(User::getPhone, keyword);
        }

        Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        // 脱敏处理：不返回openId和密码，但返回registerType供前端展示注册方式
        page.getRecords().forEach(u -> {
            u.setOpenId(null);
            u.setLoginPassword(null);
        });
        return ServerResponseEntity.success(page);
    }

    /**
     * 修改用户状态（禁用/启用）
     */
    @PutMapping("/status")
    public ServerResponseEntity<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());

        User user = userService.getById(id);
        if (user == null) {
            return ServerResponseEntity.fail("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return ServerResponseEntity.success();
    }
}
