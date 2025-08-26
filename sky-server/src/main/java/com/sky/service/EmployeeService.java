package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    public void save(EmployeeDTO employeeDTO);

    public PageResult pageQuery(String name, Integer page, Integer pageSize);

    void modifyStatus(Integer status, Long id);

    Employee selectById(Long id);

    void modifyInfo(EmployeeDTO employeeDTO);

    void modifyPassword(PasswordEditDTO passwordEditDTO);
}
