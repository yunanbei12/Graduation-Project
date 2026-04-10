package com.sportedu.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sportedu.backend.auth.CurrentUser;
import com.sportedu.backend.auth.JwtTokenService;
import com.sportedu.backend.auth.SecurityUtils;
import com.sportedu.backend.auth.dto.AdminLoginRequest;
import com.sportedu.backend.auth.dto.AdminLoginResponse;
import com.sportedu.backend.auth.dto.AdminMeResponse;
import com.sportedu.backend.auth.entity.SysAdmin;
import com.sportedu.backend.auth.entity.SysAdminRole;
import com.sportedu.backend.auth.entity.SysRole;
import com.sportedu.backend.auth.mapper.SysAdminMapper;
import com.sportedu.backend.auth.mapper.SysAdminRoleMapper;
import com.sportedu.backend.auth.mapper.SysRoleMapper;
import com.sportedu.backend.common.enums.ResultCode;
import com.sportedu.backend.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminAuthService {

    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AdminAuthService(SysAdminMapper sysAdminMapper,
                            SysAdminRoleMapper sysAdminRoleMapper,
                            SysRoleMapper sysRoleMapper,
                            PasswordEncoder passwordEncoder,
                            JwtTokenService jwtTokenService) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        SysAdmin admin = sysAdminMapper.selectOne(Wrappers.<SysAdmin>lambdaQuery()
            .eq(SysAdmin::getUsername, request.username())
            .eq(SysAdmin::getDeleted, 0)
            .last("limit 1"));
        if (admin == null || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已禁用");
        }

        List<String> roles = getRoleCodes(admin.getId());
        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminMapper.updateById(admin);

        String token = jwtTokenService.generateToken(admin.getId(), admin.getUsername(), "ADMIN", roles);
        return new AdminLoginResponse(token, admin.getId(), admin.getUsername(), admin.getRealName(), roles);
    }

    public AdminMeResponse currentAdmin() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        SysAdmin admin = getEnabledAdmin(currentUser.getUserId());
        return toAdminMeResponse(admin, getRoleCodes(admin.getId()));
    }

    private SysAdmin getEnabledAdmin(Long adminId) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null || !Integer.valueOf(0).equals(admin.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }
        return admin;
    }

    private AdminMeResponse toAdminMeResponse(SysAdmin admin, List<String> roles) {
        return new AdminMeResponse(
            admin.getId(),
            admin.getUsername(),
            admin.getRealName(),
            admin.getPhone(),
            admin.getEmail(),
            admin.getStatus(),
            roles
        );
    }

    private List<String> getRoleCodes(Long adminId) {
        List<SysAdminRole> relations = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
            .eq(SysAdminRole::getAdminId, adminId)
            .eq(SysAdminRole::getDeleted, 0));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = relations.stream().map(SysAdminRole::getRoleId).toList();
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getStatus, 1))
            .stream()
            .map(SysRole::getRoleCode)
            .toList();
    }
}
