package cn.changyou.item.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author xgl
 * @create 2019-12-25 15:15
 */
@RequestMapping("category")
public interface CategoryApi {
    @PostMapping("/{ids}")
    List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids);
}
