package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Transactional
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);

        //设置用户Id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //判断用户购物车有没有该菜品
        List<ShoppingCart> list = shoppingCartMapper.getByShoppingCart(shoppingCart);

        //有该菜品加1
        if(list!=null&&list.size()>0){
            ShoppingCart dish = list.get(0);
            dish.setNumber(dish.getNumber()+1);
            shoppingCartMapper.modify(dish);
        }
        else{
            //无该菜品
            //判断是菜品还是套餐
            //往不同的表中查数据
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId!=null){
                Dish dish = dishMapper.queryById(dishId);
                //以前的数据会丢失
                /*shoppingCart = shoppingCart.builder()
                        .name(dish.getName())
                        .amount(dish.getPrice())
                        .image(dish.getImage())
                        .build();*/
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else{
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.queryById(setmealId);

                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            //新增一条数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.getByShoppingCart(shoppingCart);

        return list;
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void deleteOneDish(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.getByShoppingCart(shoppingCart);

        if(list!=null && list.size()>0){
            ShoppingCart shoppingCart1 = list.get(0);

            if(shoppingCart1.getNumber()==1){
                shoppingCartMapper.deleteByShoppingCart(shoppingCart1);
            }else{
                shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
                shoppingCartMapper.modify(shoppingCart1);
            }

        }
    }


}
