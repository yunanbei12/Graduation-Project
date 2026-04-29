package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.AiKnowledge;
import com.kinetic.sports.service.AiKnowledgeService;
import com.kinetic.sports.service.mapper.AiKnowledgeMapper;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge> implements AiKnowledgeService {
}
