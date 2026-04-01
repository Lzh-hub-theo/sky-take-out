package com.sky.mapper;

import com.sky.annotation.AutoFillAnno;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @AutoFillAnno(value = OperationType.INSERT)
    void insert(Dish dish);

    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    List<Integer> queryStatusByIds(List<Long> ids);

    void deleteBatch(List<Long> ids);

    @AutoFillAnno(value=OperationType.UPDATE)
    void modify(Dish dish);

    @Select("select * from dish where id = #{id}")
    Dish queryById(Long id);

    List<Dish> queryBatchByCategoryId(Dish dish);

    @Select("select count(*) from dish where status=#{status}")
    Integer queryCountByStatus(Integer status);
}

