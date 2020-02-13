package cn.changyou.order.mapper;

import cn.changyou.order.pojo.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderStatusMapper extends Mapper<OrderStatus> {

    @Update("UPDATE cy_order_status SET status = 6 WHERE order_id = #{orderId} ")
    void updateOrderStatusById(Long orderId);

    @Select("select order_id from cy_order_status where status =#{status} ")
    List<Long> queryStatus(@Param("status") Integer status);
}
