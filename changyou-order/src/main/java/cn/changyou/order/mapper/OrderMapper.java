package cn.changyou.order.mapper;

import cn.changyou.order.pojo.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

public interface OrderMapper extends Mapper<Order> {

    List<Order> queryOrderList(
            @Param("userId") Long userId,
            @Param("status") Integer status);

    @Select("select count(1) from cy_order where create_time between #{time} and #{time1} ")
    int orderCount(@Param("time") String time,@Param("time1") String time1);


    @Select("select actual_pay from cy_order where create_time between #{time} and #{time1}")
    List<BigDecimal> orderSalesVolume(@Param("time") String time, @Param("time1") String time1);
}
