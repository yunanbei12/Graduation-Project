package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.AiMessage;
import com.kinetic.sports.service.AiMessageService;
import com.kinetic.sports.service.mapper.AiMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements AiMessageService {
}
