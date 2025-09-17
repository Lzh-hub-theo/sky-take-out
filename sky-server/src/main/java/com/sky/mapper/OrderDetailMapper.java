package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void InsertBatch(List<OrderDetail> list);

    List<OrderDetail> listByOrderIds(List<Long> arr);
}
