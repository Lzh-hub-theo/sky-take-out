package com.sky.controller.admin;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags="分类相关接口")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    /**
     * 分类分页查询
     * @param name
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result categoryPageQuery(@RequestParam(required = false)String name,@RequestParam(required = false)Integer type,@RequestParam Integer page,@RequestParam Integer pageSize){
        log.info("分类分页查询:{},{},{},{}",name,type,page,pageSize);
        PageResult pageResult = categoryService.pageQuery(name, type, page, pageSize);
        return Result.success(pageResult);
    }

}
