package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> querySetmealIdByDishIds(List<Long> ids);

    @Select("select copies, description, image d.name from setmeal_dish sd left join dish d on sd.dish_id=d.id where setmeal_id=#{id}")
    List<DishItemVO> queryDishBySetmealId(Long id);
}
