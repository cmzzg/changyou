package cn.changyou.order.api;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author GM
 * @create 2020-02-02 10:58
 */
public interface OrderApi {
    @GetMapping("count")
    int orderCount();
}
