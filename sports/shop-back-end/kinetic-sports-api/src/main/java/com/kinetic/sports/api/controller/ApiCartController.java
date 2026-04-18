package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Cart;
import com.kinetic.sports.bean.model.Prod;
import com.kinetic.sports.bean.model.Sku;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.CartService;
import com.kinetic.sports.service.ProdService;
import com.kinetic.sports.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class ApiCartController {

    private final CartService cartService;
    private final ProdService prodService;
    private final SkuService skuService;

    @GetMapping("/list")
    public ServerResponseEntity<List<Cart>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Cart> cartList = cartService.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getCreateTime)
        );
        // 填充商品和SKU信息
        cartList.forEach(cart -> {
            Prod prod = prodService.getById(cart.getProdId());
            if (prod != null) {
                cart.setProdName(prod.getName());
                cart.setProdPic(prod.getPic());
                // 单规格商品：SKU为空时使用商品价格
                if (cart.getSkuId() != null) {
                    Sku sku = skuService.getById(cart.getSkuId());
                    if (sku != null) {
                        cart.setSkuProperties(sku.getProperties());
                        cart.setSkuPrice(sku.getPrice());
                    }
                } else {
                    cart.setSkuPrice(prod.getPrice());
                }
            }
        });
        return ServerResponseEntity.success(cartList);
    }

    @PostMapping("/add")
    public ServerResponseEntity<Void> add(@RequestBody Cart cart) {
        Long userId = StpUtil.getLoginIdAsLong();
        cart.setUserId(userId);

        // 检查是否已存在相同商品+SKU
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProdId, cart.getProdId());
        if (cart.getSkuId() != null) {
            wrapper.eq(Cart::getSkuId, cart.getSkuId());
        } else {
            wrapper.isNull(Cart::getSkuId);
        }
        Cart existCart = cartService.getOne(wrapper);
        if (existCart != null) {
            existCart.setQty(existCart.getQty() + cart.getQty());
            cartService.updateById(existCart);
        } else {
            cartService.save(cart);
        }
        return ServerResponseEntity.success();
    }

    @PutMapping("/update")
    public ServerResponseEntity<Void> updateQty(@RequestBody Cart cart) {
        Long userId = StpUtil.getLoginIdAsLong();
        Cart dbCart = cartService.getById(cart.getId());
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("购物车项不存在");
        }
        dbCart.setQty(cart.getQty());
        cartService.updateById(dbCart);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Cart dbCart = cartService.getById(id);
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            return ServerResponseEntity.fail("购物车项不存在");
        }
        cartService.removeById(id);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/clear")
    public ServerResponseEntity<Void> clear() {
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.remove(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)
        );
        return ServerResponseEntity.success();
    }

    @GetMapping("/count")
    public ServerResponseEntity<Long> count() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ServerResponseEntity.success(cartService.count(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)
        ));
    }
}
