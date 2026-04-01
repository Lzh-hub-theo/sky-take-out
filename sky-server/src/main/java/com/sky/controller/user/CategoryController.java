package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags="C端-分类接口")
@RestController("userCategoryController")
@Slf4j
@RequestMapping("/user/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    /**
     * 条件查询
     * @param type
     * @return
     */
    @ApiOperation("条件查询")
    @GetMapping("/list")
    public Result<List<Category>> queryCategory(Integer type){
        log.info("条件查询:{}",type);
        List<Category> categories = categoryService.queryByType(type);
        return Result.success(categories);
    }
}
