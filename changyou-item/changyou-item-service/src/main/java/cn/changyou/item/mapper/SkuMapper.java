package cn.changyou.item.mapper;

import cn.changyou.item.pojo.Sku;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author GM
 * @create 2020-01-04 18:22
 */
public interface SkuMapper extends Mapper<Sku> {
    @Select("select count(1) from cy_sku")
    Integer querySkuByCount();
}
