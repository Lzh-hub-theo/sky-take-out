package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFillAnno;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    //根据分类id查询套餐
    @Select("select * from setmeal where category_id = #{categoryId}")
    List<Setmeal> queryByCategoryId(Long categoryId);

    //修改指定套餐
    @AutoFillAnno(value= OperationType.UPDATE)
    /*@Update("update setmeal set " +
            "category_id = #{categoryId},name=#{name},price=#{price},status=#{status},description=#{description},image=#{image}" +
            " where id = #{id}")*/
    void modify(Setmeal setmeal);

    //查询套餐以及分类名称
    Page<SetmealVO> listWithCategoryName(SetmealPageQueryDTO setmealPageQueryDTO);

    //插入套餐
    @AutoFillAnno(value=OperationType.INSERT)
    void insert(Setmeal setmeal);

    //批量删除套餐
    void deleteBatch(Long[] ids);

    //根据id查询套餐
    @Select("select * from setmeal where id = #{id}")
    Setmeal queryById(Long id);

    //查询套餐的分类名称
    @Select("select c.name from setmeal s left join category c on s.category_id = c.id where s.id=#{id}")
    String queryCategoryNameByid(Long id);

    @Select("select count(*) from setmeal where status=#{status}")
    Integer queryCountByStatus(Integer status);
}
