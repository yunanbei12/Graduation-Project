package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.ProdCategory;
import com.kinetic.sports.service.ProdCategoryService;
import com.kinetic.sports.service.mapper.ProdCategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class ProdCategoryServiceImpl extends ServiceImpl<ProdCategoryMapper, ProdCategory> implements ProdCategoryService {
}
