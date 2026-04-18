package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.AiFeedback;
import com.kinetic.sports.service.AiFeedbackService;
import com.kinetic.sports.service.mapper.AiFeedbackMapper;
import org.springframework.stereotype.Service;

@Service
public class AiFeedbackServiceImpl extends ServiceImpl<AiFeedbackMapper, AiFeedback> implements AiFeedbackService {
}
