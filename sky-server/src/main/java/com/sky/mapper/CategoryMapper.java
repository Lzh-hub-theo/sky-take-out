package com.sky.mapper;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> pageQuery (String name,Integer type);

    //@Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
    //        "values (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void save(Category category);

    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    @Update("update category set status = #{status} where id = #{id}")
    void modifyStatus(Long id, Integer status);

    @Select("select * from category where type = #{type}")
    List<Category> queryByType(Integer type);

    @Update("update category set name = #{name},sort = #{sort},type = #{type} where id = #{id}")
    void modifyInfo(CategoryDTO categoryDTO);
}
