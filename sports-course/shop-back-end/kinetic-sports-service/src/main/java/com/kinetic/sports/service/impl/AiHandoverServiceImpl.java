package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.AiHandover;
import com.kinetic.sports.service.AiHandoverService;
import com.kinetic.sports.service.mapper.AiHandoverMapper;
import org.springframework.stereotype.Service;

@Service
public class AiHandoverServiceImpl extends ServiceImpl<AiHandoverMapper, AiHandover> implements AiHandoverService {
}
