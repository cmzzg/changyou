package cn.changyou.deal.client;

import cn.changyou.order.api.OrderApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author GM
 * @create 2020-02-02 11:50
 */
@FeignClient("order-service")
public interface OrderClient extends OrderApi{
}
