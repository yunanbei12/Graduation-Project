package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Banner;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import com.kinetic.sports.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        return ServerResponseEntity.success(bannerService.list(wrapper).stream()
                .map(this::sanitizeBanner)
                .collect(Collectors.toList()));
    }

    private Banner sanitizeBanner(Banner banner) {
        if (banner == null) {
            return null;
        }
        if (banner.getLinkType() != null && banner.getLinkType() == 1) {
            banner.setLinkUrl(ContentSecurityUtils.normalizeMiniPagePath(banner.getLinkUrl()));
        } else if (banner.getLinkType() != null && banner.getLinkType() == 3) {
            banner.setLinkUrl(ContentSecurityUtils.normalizeExternalUrl(banner.getLinkUrl()));
        }
        return banner;
    }
}
