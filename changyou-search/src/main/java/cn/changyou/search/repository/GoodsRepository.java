package cn.changyou.search.repository;

import cn.changyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author xgl
 * @create 2019-12-25 15:55
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
