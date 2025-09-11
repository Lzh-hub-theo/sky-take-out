package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCategoryController")
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

    /**
     * 添加分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("添加分类:{}",categoryDTO);
        categoryService.save(categoryDTO);

        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result deleteById(@RequestParam Long id){
        log.info("根据id删除分类:{}",id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @param id
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result modifyStatus(@PathVariable Integer status,@RequestParam Long id){
        log.info("启用禁用分类:{},{}",id,status);
        categoryService.modifyStatus(id,status);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> queryByType(@RequestParam Integer type){
        log.info("根据类型查询分类:{}",type);
        List<Category> categories = categoryService.queryByType(type);
        return Result.success(categories);
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result modifyInfo(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类:{}",categoryDTO);
        categoryService.modifyInfo(categoryDTO);
        return Result.success();
    }

}
