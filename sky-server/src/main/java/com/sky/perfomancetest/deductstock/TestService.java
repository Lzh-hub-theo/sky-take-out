package com.sky.perfomancetest.deductstock;

import com.sky.context.BaseContext;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShoppingCartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    public boolean noadam(){
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByShoppingCart(shoppingCart);
        for (ShoppingCart cart: shoppingCartList){
            dishMapper.batchDeductStockNoAdam(cart.getDishId(), cart.getNumber());
        }
        return true;
    }

    public boolean adam(){
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByShoppingCart(shoppingCart);
        Map<Long, Integer> dishMap = shoppingCartList.stream()
                .collect(Collectors.toMap(
                        ShoppingCart::getDishId,
                        ShoppingCart::getNumber
                ));
        dishMapper.batchDeductStock(dishMap);
        return true;
    }
}
