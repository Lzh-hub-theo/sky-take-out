package com.sky.mapper;

import com.sky.annotation.AutoFillAnno;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper {

    @AutoFillAnno(value= OperationType.INSERT)
    void insert(Dish dish);
}
