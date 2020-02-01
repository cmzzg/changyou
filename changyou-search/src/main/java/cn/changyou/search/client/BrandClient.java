package cn.changyou.search.client;

import cn.changyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author xgl
 * @create 2019-12-25 15:30
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
