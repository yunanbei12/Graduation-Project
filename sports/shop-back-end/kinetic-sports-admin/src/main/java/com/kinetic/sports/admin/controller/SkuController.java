package com.kinetic.sports.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Sku;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sku")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @GetMapping("/list")
    public ServerResponseEntity<List<Sku>> list(@RequestParam Long prodId) {
        return ServerResponseEntity.success(skuService.list(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProdId, prodId)
        ));
    }

    @GetMapping("/detail/{id}")
    public ServerResponseEntity<Sku> detail(@PathVariable Long id) {
        return ServerResponseEntity.success(skuService.getById(id));
    }

    @PostMapping
    public ServerResponseEntity<Void> save(@RequestBody Sku sku) {
        skuService.save(sku);
        return ServerResponseEntity.success();
    }

    @PutMapping
    public ServerResponseEntity<Void> update(@RequestBody Sku sku) {
        skuService.updateById(sku);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        skuService.removeById(id);
        return ServerResponseEntity.success();
    }

    @PostMapping("/batch")
    public ServerResponseEntity<Void> saveBatch(@RequestBody List<Sku> skuList) {
        skuService.saveBatch(skuList);
        return ServerResponseEntity.success();
    }
}
