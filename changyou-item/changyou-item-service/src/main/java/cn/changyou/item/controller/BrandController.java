package cn.changyou.item.controller;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.item.pojo.Brand;
import cn.changyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author GM
 * @create 2019-12-26 17:12
 */
@Controller
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandservice;

    public void setBrandservice(BrandService brandservice) {
        this.brandservice = brandservice;
    }

    /**
     * @param page   第几页,默认是1
     * @param rows   每页显示多少行,默认是5
     * @return 成功或失败的状态码
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandsBypage( @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "5") Integer rows) {
        PageResult<Brand> result = this.brandservice.querySpuByPage(page, rows);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增品牌
     *
     * @param brand 品牌实体
     * @param cids  分类id的集合
     * @return 成功或失败的状态码
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {

        int i = this.brandservice.saveBrand(brand, cids);
        if (i <= 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 修改品牌信息
     *
     * @param brand 品牌实体类
     * @param cids  分类id的集合
     * @return 成功或失败的状态码
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        int i = this.brandservice.updateBrand(brand, cids);
        if (i < 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 删除品牌
     *
     * @param bids 品牌id的集合
     * @return 成功或失败的状态码
     */
    @DeleteMapping("{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") List<Long> bids) {
        int i = this.brandservice.deleteBrand(bids);
        if (i < 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 查询品牌名称
     *
     * @param cid 分类id
     * @return 品牌
     */
    @GetMapping("cid/{cid}")
    public List<Brand> queryBrandById(@PathVariable("cid") Long cid){
        List<Brand> brands = brandservice.queryBrandById(cid);
        System.out.println(cid);
        return brands;
    }
}
