package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品
 */
@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags="菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        String key = "dish_"+dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("批量删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品:{}",ids);
        dishService.deleteBatch(ids);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> queryById(@PathVariable Long id){
        log.info("根据id查询菜品:{}",id);
        DishVO dishVO = dishService.queryById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishVO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result modify(@RequestBody DishVO dishVO){
        log.info("修改菜品:{}",dishVO);
        dishService.modifyWithFlavor(dishVO);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("菜品起售、停售")
    @PostMapping("/status/{status}")
    public Result modifyStatus(@PathVariable Integer status,Long id){
        log.info("菜品起售、停售:{},{}",status,id);
        Dish dish = Dish.builder().id(id).status(status).build();
        dishService.modifyStatus(dish);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> listByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> list = dishService.listByCategoryId(categoryId);

        return Result.success(list);
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
