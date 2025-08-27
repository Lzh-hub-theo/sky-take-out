package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult pageQuery(String name, Integer type, Integer page, Integer pageSize);

    void save(CategoryDTO categoryDTO);

    void deleteById(Long id);

    void modifyStatus(Long id, Integer status);

    List<Category> queryByType(Integer type);

    void modifyInfo(CategoryDTO categoryDTO);
}
