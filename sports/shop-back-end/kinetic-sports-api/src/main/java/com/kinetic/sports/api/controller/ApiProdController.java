package com.kinetic.sports.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.Prod;
import com.kinetic.sports.bean.model.ProdCategory;
import com.kinetic.sports.bean.model.Sku;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import com.kinetic.sports.service.ProdService;
import com.kinetic.sports.service.ProdCategoryService;
import com.kinetic.sports.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prod")
@RequiredArgsConstructor
public class ApiProdController {

    private final ProdService prodService;
    private final ProdCategoryService prodCategoryService;
    private final SkuService skuService;

    @GetMapping("/list")
    public ServerResponseEntity<Page<Prod>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<Prod> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Prod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prod::getStatus, 1);
        if (categoryId != null) wrapper.eq(Prod::getCategoryId, categoryId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Prod::getName, keyword)
                    .or().like(Prod::getDescription, keyword));
        }
        wrapper.orderByDesc(Prod::getSales);
        Page<Prod> result = prodService.page(page, wrapper);
        if (result.getRecords() != null) {
            result.setRecords(result.getRecords().stream().map(this::sanitizeProd).collect(Collectors.toList()));
        }
        return ServerResponseEntity.success(result);
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Prod> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(sanitizeProd(prodService.getById(id)));
    }

    @GetMapping("/sku/list")
    public ServerResponseEntity<List<Sku>> skuList(@RequestParam Long prodId) {
        return ServerResponseEntity.success(skuService.list(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProdId, prodId)));
    }

    @GetMapping("/category/list")
    public ServerResponseEntity<List<ProdCategory>> categoryList() {
        return ServerResponseEntity.success(prodCategoryService.list(
                new LambdaQueryWrapper<ProdCategory>().eq(ProdCategory::getStatus, 1).orderByAsc(ProdCategory::getSort)));
    }

    private Prod sanitizeProd(Prod prod) {
        if (prod == null) {
            return null;
        }
        prod.setDetail(ContentSecurityUtils.sanitizeRichText(prod.getDetail()));
        return prod;
    }
}
