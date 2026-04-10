package com.sportedu.backend.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportedu.backend.auth.entity.SysAdmin;
import com.sportedu.backend.auth.entity.SysAdminRole;
import com.sportedu.backend.auth.entity.SysRole;
import com.sportedu.backend.auth.mapper.SysAdminMapper;
import com.sportedu.backend.auth.mapper.SysAdminRoleMapper;
import com.sportedu.backend.auth.mapper.SysRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminInitializer(SysAdminMapper sysAdminMapper,
                                   SysRoleMapper sysRoleMapper,
                                   SysAdminRoleMapper sysAdminRoleMapper,
                                   PasswordEncoder passwordEncoder) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleCode, "SUPER_ADMIN")
            .eq(SysRole::getDeleted, 0)
            .last("limit 1"));
        if (role == null) {
            role = new SysRole();
            role.setRoleCode("SUPER_ADMIN");
            role.setRoleName("超级管理员");
            role.setStatus(1);
            role.setDeleted(0);
            sysRoleMapper.insert(role);
            log.info("Initialized default role: SUPER_ADMIN");
        }

        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
            .eq(SysAdmin::getUsername, "admin")
            .eq(SysAdmin::getDeleted, 0)
            .last("limit 1"));
        if (admin == null) {
            admin = new SysAdmin();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            admin.setDeleted(0);
            sysAdminMapper.insert(admin);
            log.info("Initialized default admin account: admin / 123456");
        }

        Long relationCount = sysAdminRoleMapper.selectCount(new LambdaQueryWrapper<SysAdminRole>()
            .eq(SysAdminRole::getAdminId, admin.getId())
            .eq(SysAdminRole::getRoleId, role.getId())
            .eq(SysAdminRole::getDeleted, 0));
        if (relationCount == 0) {
            SysAdminRole adminRole = new SysAdminRole();
            adminRole.setAdminId(admin.getId());
            adminRole.setRoleId(role.getId());
            adminRole.setDeleted(0);
            sysAdminRoleMapper.insert(adminRole);
        }
    }
}
