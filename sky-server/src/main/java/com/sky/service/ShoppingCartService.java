package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看单个用户的购物车
     * @return
     */
    List<ShoppingCart> list();

    void clean();

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    void deleteOneDish(ShoppingCartDTO shoppingCartDTO);
}
