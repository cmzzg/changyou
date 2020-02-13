package cn.changyou.user.api;

import cn.changyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xgl
 * @create 2020-01-03 23:46
 */
public interface UserApi {
    @GetMapping("query")
    User queryUser(@RequestParam("username") String username, @RequestParam("password") String password);

    @GetMapping("count/user")
    Integer queryUserCount();

    @GetMapping("count/day/user")
    Integer queryUserByCreatedDay();

    @GetMapping("count/old/user")
    Integer queryUserByCreatedOld();

    @GetMapping("count/month/user")
    Integer queryUserByCreadMonth();
}
