package cn.changyou.item.service;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.item.bo.SpuBo;
import cn.changyou.item.mapper.*;
import cn.changyou.item.pojo.Sku;
import cn.changyou.item.pojo.Spu;
import cn.changyou.item.pojo.SpuDetail;
import cn.changyou.item.pojo.Stock;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author GM
 * @create 2020-01-03 14:54
 */

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    /**
     * 分页查询所有的商品
     *
     * @param saleable 是否上架
     * @param page     当前页码,默认是1
     * @param rows     每页显示多少行
     * @return 商品信息
     */
    public PageResult<SpuBo> querySpuBoByPage(Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        PageHelper.startPage(page, rows);

        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));

            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBos.add(spuBo);
        });

        return new PageResult<>(pageInfo.getTotal(), spuBos);

    }

    @Transactional
    /**
     * 新增商品
     *
     * @param spu SpuBo的实体类
     */
    public int save(SpuBo spu) {
        // 保存spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int i = this.spuMapper.insert(spu);
        // 保存spu详情
        spu.getSpuDetail().setSpuId(spu.getId());
        int i1 = this.spuDetailMapper.insert(spu.getSpuDetail());
        saveSkuAndStock(spu.getSkus(), spu.getId());
        if (i < 0 || i1 < 0) {
            return -1;
        }
        return 1;
    }

    /**
     * 保存sku和库存信息
     *
     * @param skus  sku商品集合
     * @param spuId spuid
     */
    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    /**
     * 通过spuid删除该商品所有信息
     *
     * @param spuid spu表的商品id
     * @return 受影响行数
     */
    public int deleteSpu(Long spuid) {
        int i = spuMapper.deleteByPrimaryKey(spuid);
        int i1 = spuDetailMapper.deleteByPrimaryKey(spuid);
        Sku sku = new Sku();
        sku.setSpuId(spuid);
        List<Sku> select = skuMapper.select(sku);
        Stock stock = new Stock();
        int i3 = -1;
        for (Sku sku1 : select) {
            stock.setSkuId(sku1.getId());
            i3 = stockMapper.delete(stock);
        }
        int i2 = skuMapper.delete(sku);
        if (i > 0 && i1 > 0 && i2 > 0 && i3 > 0) {
            return 1;
        }
        return -1;
    }

    /**
     * 查询商品详细信息
     *
     * @param spuId spu表的商品id
     * @return 属于spu类中的对应的sku商品
     */
    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            // 同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    /**
     * 修改商品信息
     *
     * @param spu 与相关商品的所有信息
     * @return
     */
    @Transient
    public int updateGoods(SpuBo spu) {
        //修改spu
        spu.setLastUpdateTime(new Date());
        int i = this.spuMapper.updateByPrimaryKeySelective(spu);
        //修改商品详情
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        int i1 = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        List<Sku> skus = spu.getSkus();
        updateSkuAndStock(skus);
        return 1;
    }

    /**
     * 修改sku和库存
     * @param skus 某个商品下具有的不同款式的商品集合
     */
    private void updateSkuAndStock(List<Sku> skus){
        for (Sku sku : skus) {
            if(!sku.getEnable()){
                continue;
            }
            sku.setLastUpdateTime(new Date());
            skuMapper.updateByPrimaryKeySelective(sku);
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockMapper.updateByPrimaryKeySelective(stock);
            }
    }

    /**
     * 商品上下架
     * @param spuId  spu商品id
     * @param type 要改变得状态
     */
    public void updateStand(Long spuId,Boolean type) {
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setSaleable(type);
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 通过id修改是某个商品显示在首页以及显示在哪个位置
     * @param id spuid
     * @param sort 排序
     * @param isHomeGoods 是否显示在首页
     */
    public void updatesortById(Long id, Long sort, Boolean isHomeGoods) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSort(sort);
        spu.setIsHomeGoods(isHomeGoods);
        Example example =new Example(Spu.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sort",sort);
        Spu spu1 = spuMapper.selectOneByExample(example);
        if (spu1 != null){
            spu1.setSort(0L);
            spu1.setIsHomeGoods(false);
            spuMapper.updateByPrimaryKeySelective(spu1);
        }
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 计算某个商品的评论数量 新增评论
     * @param spu
     */
    public void updateByRemakNum(Spu spu){
        Spu spu1 = spuMapper.selectOne(spu);
        Long l = spu1.getRemakeNum() + 1;
        Spu spu2 = new Spu();
        spu2.setId(spu.getId());
        spu2.setRemakeNum(l);
        spuMapper.updateByPrimaryKeySelective(spu2);
    }

    /**
     * 计算某个商品的评论数量 删除评论
     * @param id
     */
    public void deleteCommentById(Long id){
        Spu spu = new Spu();
        spu.setId(id);
        Spu spu1 = spuMapper.selectByPrimaryKey(spu);
        Long remakeNum = spu1.getRemakeNum() - 1;
        Spu spu2 = new Spu();
        spu2.setId(spu.getId());
        spu2.setRemakeNum(remakeNum);
        spuMapper.updateByPrimaryKeySelective(spu2);
    }

    /**
     * 通过spuid查询spu商品
     * @param spuId
     * @return
     */
    public Spu querySpuById(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        return spu;
    }

    /**
     * 根据数据是否在首先显示进行数据查询和排序
     *
     * @param page 当前页码
     * @param rows 显示行数
     * @return 商品集合
     */
    public PageResult<List<Spu>> querySpuByIdsBySort(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);

        Example example = new Example(Spu.class);
        example.setOrderByClause("sort ASC");
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isHomeGoods", "1");
        List<Spu> spus = spuMapper.selectByExample(example);

        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        return new PageResult(pageInfo.getTotal(), spus);
    }

    /**
     * 根据商品上下架状态统计商品总数
     * @param saleable 商品上下架状态
     * @return
     */
    public int querySpuCount(boolean saleable) {
        Spu spu = new Spu();
        spu.setSaleable(saleable);
        Integer i = spuMapper.selectCount(spu);
        return i;
    }

    /**
     * 统计sku商品总数
     * @return
     */
    public Integer querySkuByCount() {
        Integer i = skuMapper.querySkuByCount();
        return i;
    }

    /**
     * 统计库存为0的商品数量
     * @return
     */
    public Integer querySkuStock() {
        Stock stock =new Stock();
        stock.setStock(0);
        Integer i = stockMapper.selectCount(stock);
        return i;
    }

    /**
     * 查询所有库存为0的商品
     * @return
     */
    public List<Sku> querySkuByStock(){
        Stock stock =new Stock();
        stock.setStock(0);
        Sku sku = new Sku();
        List<Sku> skus = new ArrayList<>();
        List<Stock> stocks = stockMapper.select(stock);
        for (Stock stock1 : stocks) {
            sku.setId(stock1.getSkuId());
            Sku sku1 = skuMapper.selectByPrimaryKey(sku);
            skus.add(sku1);
        }
        return skus;
    }

    /**
     * 查询所有库存小于10的商品详情
     * @return
     */
    public List<Sku> queryStockByCountSku() {
        List<String> stocks = stockMapper.queryStockByCountSku();
        List<Sku> skus = new ArrayList<>();
        for (String stock1 : stocks) {
            Sku sku = skuMapper.selectByPrimaryKey(stock1);
            skus.add(sku);
        }
        return skus;
    }

    public Integer queryStockByCount() {
        return stockMapper.queryStockByCount();
    }

    public void setSpuMapper(SpuMapper spuMapper) {
        this.spuMapper = spuMapper;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setBrandMapper(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    public void setSkuMapper(SkuMapper skuMapper) {
        this.skuMapper = skuMapper;
    }

    public void setStockMapper(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    public void setSpuDetailMapper(SpuDetailMapper spuDetailMapper) {
        this.spuDetailMapper = spuDetailMapper;
    }

    public Sku querySkuBySkuId(Long skuId) {
        return skuMapper.selectByPrimaryKey(skuId);
    }

    public SpuDetail querySpuDetailById(Long id) {
        return spuDetailMapper.selectByPrimaryKey(id);
    }



}
