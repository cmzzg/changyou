package cn.changyou.item.api;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.item.bo.SpuBo;
import cn.changyou.item.pojo.Sku;
import cn.changyou.item.pojo.Spu;
import cn.changyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xgl
 * @create 2019-12-25 15:19
 */
public interface GoodsApi {
    /**
     * 通过spuid查询spudetail,用于编辑时的商品回显
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuID(@PathVariable("spuId") Long spuId);
    /**
     * 商品列表分页展示
     * @return
     */
    @GetMapping("spu/page")
    PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );
    /**
     * 通过spuid查询下面所有的具体商品
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkusBySpuId(@RequestParam("id") Long spuId);

    @GetMapping("sku/{skuId}")
    Sku querySkuBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("spu/sort")
    PageResult<List<Spu>> querySpuByIdsBySort(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "25") Integer rows);

    @PutMapping("spu/update/remakeNum")
    Void updateByRemakNum(Spu spu);

    @DeleteMapping("spu/delete/remakeNum")
    Void deleteCommentById(@RequestParam("id") Long id);

    @GetMapping("spu/spuId")
    Spu querySpuById(Long spuId);

    @GetMapping("count/{saleable}")
    Integer querySkuCount(@PathVariable("saleable") boolean saleable);

    @GetMapping("count/stock")
    Integer queryStockByCount();

    @GetMapping("count/sku")
    Integer querySkuByCount();

    @GetMapping("stock/count")
    Integer querySkuStock();

    @GetMapping("sku/stock")
    List<Sku> querySkuByStock();

    /**
     * 1
     * @return
     */
    @GetMapping("stock/sku")
    List<Sku> queryStockByCountSku();
}
