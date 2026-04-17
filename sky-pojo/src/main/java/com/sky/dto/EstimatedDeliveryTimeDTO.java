package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EstimatedDeliveryTimeDTO {
    @NotBlank(message = "店铺ID不能为空")
    private String shopId;

    @NotBlank(message = "收货地址不能为空")
    private String customerAddress;
}
