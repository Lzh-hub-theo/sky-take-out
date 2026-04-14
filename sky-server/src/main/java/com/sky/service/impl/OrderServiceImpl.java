package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.BaiduGeoUtils;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private BaiduGeoUtils baiduGeoUtils;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //增强程序的健壮性：先判断购物车和地址簿是否有相应的数据
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook address = addressBookMapper.getById(addressBookId);
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //检查是否超出配送范围
        baiduGeoUtils.checkOutOfRange(address.getCityName() + address.getDistrictName() + address.getDetail());

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByShoppingCart(shoppingCart);

        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //数据插入下单和下单明细表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        //LocalDateTime 和 Long类型不能进行自动类型转换
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(address.getPhone());
        orders.setAddress(address.getProvinceName() + address.getCityName() + address.getDistrictName() + address.getDetail());
        orders.setConsignee(address.getConsignee());

        orderMapper.insert(orders);
        Long orderId = orders.getId();

        List<OrderDetail> list = new ArrayList<>();
        for (ShoppingCart sc : shoppingCartList) {
            OrderDetail detail = new OrderDetail();
            detail.setName(sc.getName());
            detail.setOrderId(orderId);
            detail.setDishId(sc.getDishId());
            detail.setSetmealId(sc.getSetmealId());
            detail.setDishFlavor(sc.getDishFlavor());
            detail.setNumber(sc.getNumber());
            detail.setAmount(sc.getAmount());
            detail.setImage(sc.getImage());
            list.add(detail);
        }

        orderDetailMapper.InsertBatch(list);
        //返回数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/

        // 模拟微信支付转发假数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // 调用updateStatus，用于替换微信支付更新数据库状态的问题
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Orders order = Orders.builder()
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .number(orderNumber)
                .build();
        orderMapper.updateStatus(order);

        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //用户完成订单后提醒商家接单
        order = orderMapper.getByNumber(orderNumber);
        //type: 1表示来单提醒，2表示用户催单
        //Map.of("type",1,"orderId",order.getId(),"content","订单号："+orderNumber);
        Map<Object, Object> objectMap = new HashMap<>();
        objectMap.put("type", 1);
        objectMap.put("orderId", order.getId());
        objectMap.put("content", "订单号：" + orderNumber);
        String json = JSON.toJSONString(objectMap);
        webSocketServer.sendMessageToAll(json);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    @Transactional
    // TODO 后续可以尝试对for循环里面的查询改为单道查询
    public PageResult listWithDetails(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //查询到部分的单
        List<Orders> list = orderMapper.list(ordersPageQueryDTO);

        Page<Orders> p = (Page<Orders>) list;

        List<OrderVO> orderAndDetailVOList = new ArrayList<>();

        for (Orders order : list) {
            OrderVO obj = new OrderVO();
            List<Long> ids = Arrays.asList(order.getId());

            List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderIds(ids);

            BeanUtils.copyProperties(order, obj);
            obj.setOrderDetailList(orderDetailList);

            StringBuilder sb = new StringBuilder();
            for (OrderDetail orderDetail : orderDetailList) {
                sb.append(orderDetail.getName());
            }
            obj.setOrderDishes(sb.toString());

            orderAndDetailVOList.add(obj);
        }

        PageResult pageResult = new PageResult();
        pageResult.setTotal(p.getTotal());
        pageResult.setRecords(orderAndDetailVOList);

        return pageResult;
    }

    @Override
    @Transactional
    public OrderVO getOrderDetail(Long orderId) {

        Orders orders = orderMapper.getById(orderId);

        if (orders == null) {
            throw new NoSuchElementException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<Long> ids = Arrays.asList(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderIds(ids);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.CANCELLED).payStatus(Orders.REFUND).build();
        orderMapper.update(orders);
    }

    @Override
    @Transactional
    public void repitition(Long id) {
        Orders orders = orderMapper.getById(id);

        if (orders == null) {
            throw new NoSuchElementException(MessageConstant.ORDER_NOT_FOUND);
        }

        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);

        orderMapper.insert(orders);

        List<Long> ids = Arrays.asList(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderIds(ids);
        orderDetailList.forEach(detail -> detail.setOrderId(orders.getId()));

        orderDetailMapper.InsertBatch(orderDetailList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    @Transactional
    public OrderStatisticsVO statistics() {
        Integer toBeComfirmed = orderMapper.statisticsByStatus(Map.of("status", Orders.TO_BE_CONFIRMED));
        Integer comfirmed = orderMapper.statisticsByStatus(Map.of("status", Orders.CONFIRMED));
        Integer deliveryInProgress = orderMapper.statisticsByStatus(Map.of("status", Orders.DELIVERY_IN_PROGRESS));

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .toBeConfirmed(toBeComfirmed)
                .confirmed(comfirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();

        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(order);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = Orders.builder()
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        orderMapper.update(order);
    }

    @Override
    public void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders order = Orders.builder()
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .payStatus(Orders.REFUND)
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(order);
    }

    @Override
    public void modifyStatusToDelivery(Long id) {
        Orders order = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(order);
    }

    @Override
    public void modifyStatusToComplete(Long id) {
        Orders order = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }

    /**
     * 向商家端发送消息
     *
     * @param id 订单id
     */
    @Override
    public void remind(Long id) {
        Orders orders = orderMapper.getById(id);

        Map<Object, Object> map = new HashMap<>();
        //type: 1,提醒接单 2,催单
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendMessageToAll(json);
    }
}
