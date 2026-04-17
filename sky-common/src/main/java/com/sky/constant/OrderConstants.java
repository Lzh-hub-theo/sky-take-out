package com.sky.constant;

import java.time.LocalTime;

/**
 * 订单业务常量
 */
public final class OrderConstants {

    private OrderConstants() {
        // 私有构造，防止实例化
    }

    /** 默认配送时长（分钟） */
    public static final int DELIVERY_MINUTES = 30;

    /** 营业开始时间 */
    public static final LocalTime BUSINESS_START = LocalTime.of(9, 0);

    /** 营业结束时间 */
    public static final LocalTime BUSINESS_END = LocalTime.of(22, 30);
}