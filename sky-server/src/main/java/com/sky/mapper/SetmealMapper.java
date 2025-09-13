package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFillAnno;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {
    @Select("select * from setmeal where category_id = #{categoryId}")
    List<Setmeal> queryByCategoryId(Long categoryId);

    @AutoFillAnno(value= OperationType.UPDATE)
    /*@Update("update setmeal set " +
            "category_id = #{categoryId},name=#{name},price=#{price},status=#{status},description=#{description},image=#{image}" +
            " where id = #{id}")*/
    void modify(Setmeal setmeal);

    Page<SetmealVO> listWithCategoryName(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFillAnno(value=OperationType.INSERT)
    void insert(Setmeal setmeal);

    void deleteBatch(Long[] ids);

    @Select("select * from setmeal where id = #{id}")
    Setmeal queryById(Long id);

    @Select("select c.name from setmeal s left join category c on s.category_id = c.id where s.id=#{id}")
    String queryCategoryNameByid(Long id);
}
