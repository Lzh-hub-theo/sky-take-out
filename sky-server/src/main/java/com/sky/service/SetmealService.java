package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    public List<Setmeal> queryByCategoryId(Long categoryId);

    List<DishItemVO> queryDishById(Long id);

    void modifyWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void saveWithDish(SetmealDTO setmealDTO);

    void modifyStatus(Integer status, Long id);

    void deleteBatch(Long[] ids);

    SetmealVO queryById(Long id);
}
