package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags="C端-套餐接口")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询套餐")
    @GetMapping("/list")
    public Result<List<Setmeal>> queryByCategoryId(Long categoryId){
        log.info("根据分类id查询套餐:{}",categoryId);
        List<Setmeal> setmeals = setmealService.queryByCategoryId(categoryId);
        return Result.success(setmeals);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @ApiOperation("根据套餐id查询包含的菜品")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> queryDishById(@PathVariable Long id){
        log.info("根据套餐id查询包含的菜品:{}",id);

        List<DishItemVO> dishes = setmealService.queryDishById(id);

        return Result.success(dishes);
    }
}
