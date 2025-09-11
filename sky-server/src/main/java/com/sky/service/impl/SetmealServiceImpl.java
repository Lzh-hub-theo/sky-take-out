package com.sky.service.impl;

import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    public List<Setmeal> queryByCategoryId(Long categoryId) {
        List<Setmeal> setmeals = setmealMapper.queryByCategoryId(categoryId);
        return setmeals;
    }

    @Override
    public List<DishItemVO> queryDishById(Long id) {
        List<DishItemVO> dishes = setmealDishMapper.queryDishBySetmealId(id);
        return dishes;
    }
}
