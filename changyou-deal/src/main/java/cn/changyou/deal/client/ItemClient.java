package cn.changyou.deal.client;

import cn.changyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author GM
 * @create 2020-02-03 15:15
 */
@FeignClient("item-service")
public interface ItemClient extends GoodsApi {
}
