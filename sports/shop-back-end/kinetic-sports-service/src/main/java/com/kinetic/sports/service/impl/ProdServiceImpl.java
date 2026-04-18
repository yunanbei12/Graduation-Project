package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Prod;
import com.kinetic.sports.service.ProdService;
import com.kinetic.sports.service.mapper.ProdMapper;
import org.springframework.stereotype.Service;

@Service
public class ProdServiceImpl extends ServiceImpl<ProdMapper, Prod> implements ProdService {
}
