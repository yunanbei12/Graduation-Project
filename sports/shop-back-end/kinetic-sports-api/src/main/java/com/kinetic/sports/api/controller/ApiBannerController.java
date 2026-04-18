package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Banner;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class ApiBannerController {

    private final BannerService bannerService;

    /**
     * 获取首页 Banner 列表（仅返回启用状态）
     */
    @GetMapping("/list")
    public ServerResponseEntity<List<Banner>> list(
            @RequestParam(required = false, defaultValue = "1") Integer position) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1);
        wrapper.eq(Banner::getPosition, position);
        wrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        return ServerResponseEntity.success(bannerService.list(wrapper));
    }
}
