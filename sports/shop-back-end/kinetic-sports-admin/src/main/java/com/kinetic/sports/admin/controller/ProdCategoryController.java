package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.ProdCategory;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.ProdCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prod/category")
@RequiredArgsConstructor
public class ProdCategoryController {

    private final ProdCategoryService prodCategoryService;

    @GetMapping("/list")
    public ServerResponseEntity<List<ProdCategory>> list() {
        return ServerResponseEntity.success(prodCategoryService.list(
                new LambdaQueryWrapper<ProdCategory>().orderByAsc(ProdCategory::getSort)
        ));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<ProdCategory> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(prodCategoryService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody ProdCategory category) {
        prodCategoryService.save(category);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody ProdCategory category) {
        prodCategoryService.updateById(category);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        prodCategoryService.removeById(id);
        return ServerResponseEntity.success();
    }
}
