package cn.changyou.user.api;

import cn.changyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author yb
 * @create 2020-01-12 11:19
 */
@RequestMapping

public interface QureyApi {
    @GetMapping("people")
    List<User> queryById(@RequestParam("ids") List<Long> id);
}
