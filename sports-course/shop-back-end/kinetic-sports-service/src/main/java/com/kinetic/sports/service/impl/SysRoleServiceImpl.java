package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.SysRole;
import com.kinetic.sports.service.SysRoleService;
import com.kinetic.sports.service.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
