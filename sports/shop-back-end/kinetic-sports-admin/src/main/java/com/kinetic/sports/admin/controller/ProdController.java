package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Prod;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import com.kinetic.sports.service.ProdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prod")
@RequiredArgsConstructor
public class ProdController {

    private final ProdService prodService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Prod>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId) {
        Page<Prod> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Prod> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) wrapper.eq(Prod::getCategoryId, categoryId);
        wrapper.orderByDesc(Prod::getCreateTime);
        return ServerResponseEntity.success(prodService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Prod> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(sanitizeProd(prodService.getById(id)));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Prod prod) {
        sanitizeProd(prod);
        prodService.save(prod);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Prod prod) {
        sanitizeProd(prod);
        prodService.updateById(prod);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        prodService.removeById(id);
        return ServerResponseEntity.success();
    }

    private Prod sanitizeProd(Prod prod) {
        if (prod == null) {
            return null;
        }
        prod.setName(ContentSecurityUtils.normalizeText(prod.getName()));
        prod.setDescription(ContentSecurityUtils.normalizeText(prod.getDescription()));
        prod.setDetail(ContentSecurityUtils.sanitizeRichText(prod.getDetail()));
        return prod;
    }
}
