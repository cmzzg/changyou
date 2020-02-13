package cn.changyou.item.controller;

import cn.changyou.item.pojo.Category;
import cn.changyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hashen
 * @create 2019-12-26 15:12
 */
@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询子节点
     *
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam("pid") Long pid) {
        if (pid == null || pid.longValue() < 0) {
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
        if (CollectionUtils.isEmpty(categories)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * 新增商品分类
     *
     * @param category
     */
    @PostMapping
    public ResponseEntity<Void> saveCategory(@RequestBody Category category) {
        categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除商品分类
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询商品分类
     *
     * @param id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") Long id) {
        Category category = categoryService.getCategory(id);
        return ResponseEntity.ok(category);
    }

    /**
     * 修改商品分类
     *
     * @param category
     */
    @PutMapping
    public ResponseEntity<Void> updataCategory(@RequestBody Category category) {
        categoryService.updataCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改品牌回显数据
     *
     * @param bid 品牌id
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> list = this.categoryService.queryByBrandId(bid);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{ids}")
    public List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids) {
        List<String> list = categoryService.queryNamesByIds(ids);
        return list;
    }
}
