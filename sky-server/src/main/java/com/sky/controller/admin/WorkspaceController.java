package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "工作台接口")
@Slf4j
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 查询今日运营数据
     * @return
     */
    @ApiOperation("查询今日运营数据")
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        return Result.success(workspaceService.businessData());
    }

    /**
     * 查询套餐总览
     * @return
     */
    @ApiOperation("查询套餐总览")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        return Result.success(workspaceService.overviewSetmeals());
    }

    /**
     * 查询套餐总览
     * @return
     */
    @ApiOperation("查询套餐总览")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        return Result.success(workspaceService.overviewDishes());
    }

    /**
     * 查询订单管理数据
     */
    @ApiOperation("查询订单管理数据")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders(){
        return Result.success(workspaceService.overviewOrders());
    }

}
