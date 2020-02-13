package cn.changyou.item.mapper;

import cn.changyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author GM
 * @create 2020-01-04 18:22
 */
public interface StockMapper extends Mapper<Stock> {
    @Select("select count(1) from cy_stock where stock < 10")
    Integer queryStockByCount();

    @Select("select cs.sku_id from cy_stock cs where stock < 10 ")
    List<String> queryStockByCountSku();
}
