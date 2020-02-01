package cn.changyou.search.controller;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.search.pojo.Goods;
import cn.changyou.search.pojo.SearchRequest;
import cn.changyou.search.pojo.SearchResult;
import cn.changyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xgl
 * @create 2019-12-25 19:02
 */
@Controller
public class SearchController {
    @Autowired
    private SearchService searchService;
    @PostMapping("page")
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest searchRequest){
        SearchResult result = searchService.search(searchRequest);
        if (result == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
