package com.sky.service;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {
    public List<Setmeal> queryByCategoryId(Long categoryId);

    List<DishItemVO> queryDishById(Long id);
}
