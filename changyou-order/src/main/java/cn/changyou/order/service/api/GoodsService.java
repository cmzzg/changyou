package cn.changyou.order.service.api;

import cn.changyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "leyou-gateway", path = "/api/item")
public interface GoodsService extends GoodsApi {
}
