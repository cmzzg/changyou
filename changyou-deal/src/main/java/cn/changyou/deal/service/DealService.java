package cn.changyou.deal.service;

import cn.changyou.deal.client.OrderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author GM
 * @create 2020-02-03 14:56
 */
@Service
public class DealService {

    @Autowired
    private OrderClient orderClient;

    public int orderCount() {
        int i = orderClient.orderCount();
        return i;
    }
}
