package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.CoachSettlement;
import com.kinetic.sports.service.CoachSettlementService;
import com.kinetic.sports.service.mapper.CoachSettlementMapper;
import org.springframework.stereotype.Service;

@Service
public class CoachSettlementServiceImpl extends ServiceImpl<CoachSettlementMapper, CoachSettlement> implements CoachSettlementService {
}
