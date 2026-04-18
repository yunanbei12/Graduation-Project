package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.AiSession;
import com.kinetic.sports.service.AiSessionService;
import com.kinetic.sports.service.mapper.AiSessionMapper;
import org.springframework.stereotype.Service;

@Service
public class AiSessionServiceImpl extends ServiceImpl<AiSessionMapper, AiSession> implements AiSessionService {
}
