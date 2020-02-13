package cn.changyou.deal.client;

import cn.changyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author GM
 * @create 2020-02-13 14:48
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {

}
