package com.sky.dto;

import lombok.*;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersSubmitBakDTO extends OrdersSubmitDTO implements Serializable {
    private Long userId;
    private String messageId;
}
