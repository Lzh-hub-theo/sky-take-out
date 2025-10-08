package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.OrdersReportDTO;
import com.sky.entity.Orders;
import com.sky.exception.BaseException;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end)) throw new BaseException("前端发送的json数据有误");

        //先查出所有的营业数据
        //for循环赋值
        List<SubtractTurnoverVO> list = reportMapper.getTurnoversByBeginAndEnd(begin,end);
        Map<LocalDate, Double> map = list.stream().collect(Collectors.toMap(
                subtractTurnoverVO -> subtractTurnoverVO.getTime(),
                subtractTurnoverVO -> subtractTurnoverVO.getTurnover()
        ));
        //DLsb: dateListStringBuilder
        //TLsb: turnoverListStringBuilder
        StringBuilder DLsb = new StringBuilder();
        StringBuilder TLsb = new StringBuilder();
        for(LocalDate date=begin;date.isBefore(end)||date.isEqual(end);date=date.plusDays(1)){
            if(map.containsKey(date)){
                DLsb.append(date.toString()+",");
                TLsb.append(map.get(date).toString()+",");
            }else{
                DLsb.append(date.toString()+",");
                TLsb.append("0,");
            }
        }

        DLsb.deleteCharAt(DLsb.lastIndexOf(","));
        TLsb.deleteCharAt(TLsb.lastIndexOf(","));

        return TurnoverReportVO.builder()
                .dateList(DLsb.toString())
                .turnoverList(TLsb.toString())
                .build();
    }

    /**
     * 用户统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    @Transactional
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end)) throw new BaseException("前端发送的json数据有误");
        List<SubtractUserVO> list = reportMapper.getUserCntByBeginAndEnd(begin,end);
        Long totalCnt = reportMapper.getSingleUserTotalCntByTime(begin.minusDays(1));

        Map<LocalDate, Long> map = list.stream().collect(Collectors.toMap(
                subtractUserVO -> subtractUserVO.getTime(),
                subtractUserVO -> subtractUserVO.getNum()
        ));
        //DLsb: dateListStringBuilder
        //NULsb: newUserListStringBuilder
        //TULsb: totalUserListStringBuilder
        StringBuilder DLsb = new StringBuilder();
        StringBuilder NULsb = new StringBuilder();
        StringBuilder TULsb = new StringBuilder();

        for(LocalDate date=begin;date.isBefore(end)|| date.isEqual(end);date=date.plusDays(1)){
            DLsb.append(date.toString()+",");
            if (map.containsKey(date)){
                NULsb.append(map.get(date).toString()+",");
                totalCnt+=map.get(date);
                TULsb.append(totalCnt.toString()+",");
            }else{
                NULsb.append("0,");
                TULsb.append(totalCnt+",");
            }
        }
        DLsb.deleteCharAt(DLsb.lastIndexOf(","));
        NULsb.deleteCharAt(NULsb.lastIndexOf(","));
        TULsb.deleteCharAt(TULsb.lastIndexOf(","));

        return UserReportVO.builder()
                .dateList(DLsb.toString())
                .newUserList(NULsb.toString())
                .totalUserList(TULsb.toString())
                .build();
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    @Transactional
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end)) throw new BaseException("前端发送的json数据有误");
        Integer totalCnt = reportMapper.getSingleTotalOrderCntByBeginAndEnd(begin,end);
        Integer validCnt = reportMapper.getSingleValidOrderCntByBeginAndEnd(begin,end);
        Double orderCompletionRate=1.0*validCnt/totalCnt;

        Map<LocalDate, Integer> totalMap = reportMapper.getTotalOrderCntByBeginAndEnd(begin, end);
        Map<LocalDate, Integer> validMap = reportMapper.getValidOrderCntByBeginAndEnd(begin, end);

        StringBuilder DLsb = new StringBuilder();//dateListStringBuilder
        StringBuilder TOCLsb = new StringBuilder();//totalOrderCountListStringBuilder
        StringBuilder VOCLsb = new StringBuilder();//validOrderCountListStringBuilder
        for(LocalDate date=begin;!date.isAfter(end);date=date.plusDays(1)){
            DLsb.append(date.toString()+",");
            if(totalMap.containsKey(date)){
                TOCLsb.append(totalMap.get(date).toString()+",");
                if(validMap.containsKey(date)){
                    VOCLsb.append(validMap.get(date).toString()+",");
                }else{
                    VOCLsb.append("0,");
                }
            }else{
                TOCLsb.append("0,");
                VOCLsb.append("0,");
            }
        }
        DLsb.deleteCharAt(DLsb.lastIndexOf(","));
        TOCLsb.deleteCharAt(TOCLsb.lastIndexOf(","));
        VOCLsb.deleteCharAt(VOCLsb.lastIndexOf(","));

        return OrderReportVO.builder()
                .totalOrderCount(totalCnt)
                .validOrderCount(validCnt)
                .orderCompletionRate(orderCompletionRate)
                .dateList(DLsb.toString())
                .orderCountList(TOCLsb.toString())
                .validOrderCountList(VOCLsb.toString())
                .build();
    }

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end)) throw new BaseException("前端发送的json数据有误");

        OrdersReportDTO ordersReportDTO = OrdersReportDTO.builder().begin(begin).end(end).status(Orders.COMPLETED).build();

        List<Map<String,Object>> list=reportMapper.top10(ordersReportDTO);

        list.sort((m1,m2)->{
            return (int)((Long)m2.get("number") - (Long)m1.get("number"));
        });

        StringBuilder nameListStringBuilder = new StringBuilder();
        StringBuilder numberListStringBuilder = new StringBuilder();

        for(Map<String,Object>map:list){
            nameListStringBuilder.append(map.get("name")+",");
            numberListStringBuilder.append(map.get("number")+",");
        }
        nameListStringBuilder.deleteCharAt(nameListStringBuilder.lastIndexOf(","));
        numberListStringBuilder.deleteCharAt(numberListStringBuilder.lastIndexOf(","));

        SalesTop10ReportVO salesTo10ReportVO = SalesTop10ReportVO.builder()
                .nameList(nameListStringBuilder.toString())
                .numberList(numberListStringBuilder.toString())
                .build();

        /*Map<String,Map<String,Object>> map = reportMapper.top10(ordersReportDTO);

        StringBuilder nameListStringBuilder = new StringBuilder();
        StringBuilder numberListStringBuilder = new StringBuilder();

        Set<String> keys = map.keySet();
        for(String key:keys){
            nameListStringBuilder.append(key+",");
            numberListStringBuilder.append(map.get(key).get("count(name)")+",");
        }
        nameListStringBuilder.deleteCharAt(nameListStringBuilder.lastIndexOf(","));
        numberListStringBuilder.deleteCharAt(numberListStringBuilder.lastIndexOf(","));

        SalesTop10ReportVO salesTo10ReportVO = SalesTop10ReportVO.builder()
                .nameList(nameListStringBuilder.toString())
                .numberList(numberListStringBuilder.toString())
                .build();*/

        return salesTo10ReportVO;
    }
}

