package com.sky.mapper;

import com.sky.annotation.AutoFillAnno;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> pageQuery (String name,Integer type);

    @AutoFillAnno(OperationType.INSERT)
    //@Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
    //        "values (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void save(Category category);

    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    @AutoFillAnno(OperationType.UPDATE)
    @Update("update category set status = #{status},update_time=#{updateTime},update_user=#{updateUser} where id = #{id}")
    void modifyStatus(Category category);

    @Select("select * from category where type = #{type}")
    List<Category> queryByType(Integer type);

    @AutoFillAnno(OperationType.UPDATE)
    @Update("update category set name = #{name},sort = #{sort},type = #{type},update_time=#{updateTime},update_user=#{updateUser} where id = #{id}")
    void modifyInfo(Category category);
}
