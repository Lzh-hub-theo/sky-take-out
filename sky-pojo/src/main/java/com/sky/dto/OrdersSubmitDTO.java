package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersSubmitDTO extends OrderSubmitBaseDTO implements Serializable {
    // 购物车项列表
    private List<CartItemDTO> cartItems;
}
