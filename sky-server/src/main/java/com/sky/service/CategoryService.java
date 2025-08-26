package com.sky.service;

import com.sky.result.PageResult;

public interface CategoryService {
    PageResult pageQuery(String name, Integer type, Integer page, Integer pageSize);
}
