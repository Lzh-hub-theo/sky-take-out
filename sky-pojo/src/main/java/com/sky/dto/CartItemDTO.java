package com.sky.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long dishId;       // 菜品ID
    private Long setmealId;    // 套餐ID
    private Integer stock;     // 数量
}
