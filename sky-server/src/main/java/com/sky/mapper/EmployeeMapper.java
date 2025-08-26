package com.sky.mapper;

import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("insert into employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)" +
            "values (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void save(Employee emp);

    //@Select("select * from employee limit #{pos},#{pageSize}")
    List<Employee> pageQuery(String name,Integer pos,Integer pageSize);

    @Select("select count(*) from employee limit #{pos},#{pageSize}")
    Long count(Integer pos, Integer pageSize);

    @Update("update employee set status = #{status} where id = #{id}")
    void modifyStatus(Integer status, Long id);

    @Select("select * from employee where id = #{id}")
    Employee selectById(Long id);

    @Update("update employee set username=#{username},name=#{name},phone=#{phone},sex=#{sex},id_number=#{idNumber},update_time=#{updateTime},update_user=#{updateUser} where id=#{id}")
    void modifyInfo(Employee emp);

    @Update("update employee set password=#{password},update_time=#{updateTime},update_user=#{updateUser} where id=#{id}")
    void modifyPassword(Employee emp);
}
