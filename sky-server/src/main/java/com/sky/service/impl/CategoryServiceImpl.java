package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageResult pageQuery(String name, Integer type, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Category> categories = categoryMapper.pageQuery(name,type);
        Page<Category> p=(Page<Category>)categories;

        PageResult pageResult = new PageResult();
        pageResult.setTotal(p.getTotal());
        pageResult.setRecords(p.getResult());

        return pageResult;
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        category.setStatus(StatusConstant.ENABLE);
        /*category.setCreateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());*/

        categoryMapper.save(category);
    }

    @Override
    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public void modifyStatus(Long id, Integer status) {
        Category category=new Category();
        /*category.setId(id);
        category.setStatus(status);*/
        categoryMapper.modifyStatus(category);
    }

    @Override
    public List<Category> queryByType(Integer type) {
        List<Category> categories = categoryMapper.queryByType(type);
        return categories;
    }

    @Override
    public void modifyInfo(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.modifyInfo(category);
    }
}
