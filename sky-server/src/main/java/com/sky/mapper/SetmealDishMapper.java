package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> querySetmealIdByDishIds(List<Long> ids);

    @Select("select copies, description, image, d.name from setmeal_dish sd left join dish d on sd.dish_id=d.id where setmeal_id=#{id}")
    List<DishItemVO> queryDishBySetmealId(Long id);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBatchBySetmealId(Long setmealId);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatchBySetmealIds(Long[] ids);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> querySetmealDishBySetmealId(Long setmealId);
}
