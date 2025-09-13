package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @ApiOperation("修改套餐")
    @PutMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result modify(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}",setmealDTO);

        setmealService.modifyWithDish(setmealDTO);

        return Result.success();
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> list(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询:{}",setmealPageQueryDTO);

        PageResult lists = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(lists);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CachePut(cacheNames = "setmeal",key = "#setmealDTO.id")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);
        setmealService.saveWithDish(setmealDTO);

        return Result.success();
    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("套餐起售、停售")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result modifyStatus(@PathVariable Integer status,Long id){
        log.info("套餐起售、停售:{},{}",status,id);

        setmealService.modifyStatus(status,id);

        return Result.success();
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result deleteBatch(@RequestParam Long[] ids){
        log.info("批量删除套餐:{}", Arrays.toString(ids));
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    @Cacheable(cacheNames = "setmeal",key = "#id")
    public Result<SetmealVO> queryById(@PathVariable Long id){
        log.info("根据id查询套餐:{}",id);
        SetmealVO setmealVO = setmealService.queryById(id);
        return Result.success(setmealVO);
    }
}
