/*
package cn.changyou.search.client;

import cn.changyou.ChangyouSearchApplication;
import cn.changyou.common.pojo.PageResult;
import cn.changyou.item.bo.SpuBo;
import cn.changyou.search.pojo.Goods;
import cn.changyou.search.repository.GoodsRepository;
import cn.changyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * @author xgl
 * @create 2019-12-25 15:56
 *//*

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChangyouSearchApplication.class)
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public void test(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
        Integer page = 1;
        Integer rows = 5;
        do {
            PageResult<SpuBo> result = goodsClient.querySpuByPage(null,null,page,rows);
            List<SpuBo> items = result.getItems();
            List<Goods> goods = items.stream().map(spuBo -> {
                try {
                    return searchService.buildGoods(spuBo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            goodsRepository.saveAll(goods);
            rows = items.size();
            page ++;
        }while (rows == 5);


    }
}
*/
