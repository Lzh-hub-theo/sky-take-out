package com.sky.mapper;

import com.sky.dto.OrdersReportDTO;
import com.sky.vo.DishNameAndNumberVO;
import com.sky.vo.SubtractTurnoverVO;
import com.sky.vo.SubtractUserVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("select cast(order_time as date) time, sum(amount) turnover from orders " +
            "where cast(order_time as date) between #{begin} and #{end} and status = 5 group by time")
    List<SubtractTurnoverVO> getTurnoversByBeginAndEnd(LocalDate begin, LocalDate end);

    @Select("select cast(create_time as date) time, count(*) num from user " +
            "where cast(create_time as date) between #{begin} and #{end} group by time;")
    List<SubtractUserVO> getUserCntByBeginAndEnd(LocalDate begin, LocalDate end);

    @Select("select count(*) from user where create_time <= #{time}")
    Long getSingleUserTotalCntByTime(LocalDate time);

    @Select("select count(*) from orders where cast(checkout_time as date) between #{begin} and #{end}")
    Integer getSingleTotalOrderCntByBeginAndEnd(LocalDate begin, LocalDate end);

    @Select("select count(*) from orders where cast(checkout_time as date) between #{begin} and #{end} and status=5")
    Integer getSingleValidOrderCntByBeginAndEnd(LocalDate begin, LocalDate end);

    @MapKey("time")
    Map<LocalDate, Integer> getTotalOrderCntByBeginAndEnd(LocalDate begin, LocalDate end);

    @MapKey("time")
    Map<LocalDate, Integer> getValidOrderCntByBeginAndEnd(LocalDate begin, LocalDate end);

    //@MapKey("name")
    //抑制 MyBatis插件的校验提示(IDEA误判)
    @SuppressWarnings({"MyBatis", "MybatisXMapperMethodInspection"})
    List<Map<String, Object>> top10(OrdersReportDTO ordersReportDTO);
}
