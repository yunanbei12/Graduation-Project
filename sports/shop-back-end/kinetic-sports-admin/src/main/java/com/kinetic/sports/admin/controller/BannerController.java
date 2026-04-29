package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Banner;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import com.kinetic.sports.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Banner>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer position,
            @RequestParam(required = false) Integer status) {
        Page<Banner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (position != null) {
            wrapper.eq(Banner::getPosition, position);
        }
        if (status != null) {
            wrapper.eq(Banner::getStatus, status);
        }
        wrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        return ServerResponseEntity.success(bannerService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Banner> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(sanitizeBanner(bannerService.getById(id)));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Banner banner) {
        sanitizeBanner(banner);
        bannerService.save(banner);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Banner banner) {
        sanitizeBanner(banner);
        bannerService.updateById(banner);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return ServerResponseEntity.success();
    }

    private Banner sanitizeBanner(Banner banner) {
        if (banner == null) {
            return null;
        }
        banner.setTitle(ContentSecurityUtils.normalizeText(banner.getTitle()));
        if (banner.getLinkType() != null && banner.getLinkType() == 1) {
            banner.setLinkUrl(ContentSecurityUtils.requireSafeMiniPagePath(banner.getLinkUrl()));
        } else if (banner.getLinkType() != null && banner.getLinkType() == 3) {
            banner.setLinkUrl(ContentSecurityUtils.requireSafeExternalUrl(banner.getLinkUrl()));
        } else {
            banner.setLinkUrl(ContentSecurityUtils.normalizeText(banner.getLinkUrl()));
        }
        return banner;
    }
}
