package cn.changyou.search.client;

import cn.changyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author xgl
 * @create 2019-12-25 15:00
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
