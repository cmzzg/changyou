package cn.changyou.search.client;

import cn.changyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author xgl
 * @create 2019-12-25 15:31
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
