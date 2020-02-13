package cn.changyou.deal.controller;

import cn.changyou.deal.client.ItemClient;
import cn.changyou.deal.client.UserClient;
import cn.changyou.item.pojo.Sku;
import cn.changyou.order.pojo.Order;
import cn.changyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author GM
 * @create 2020-02-02 10:34
 */
@Controller
@RequestMapping("/deal")
public class DealController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private UserClient userClient;

    /**
     * 统计当日订单总数
     * @return
     */
    @PostMapping("count")
    public ResponseEntity<Integer> orderCount(){
        Integer i = orderService.queryOrderCount();
        return ResponseEntity.ok(i);
    }

    /**
     *统计当日销量
     * @return
     */
    @PostMapping("day/pay")
    public  ResponseEntity<BigDecimal> querySalesVolume(){
        BigDecimal pay = orderService.querySalesVolume();
        return ResponseEntity.ok(pay);
    }

    /**
     * 统计昨日销量
     * @return
     */
    @PostMapping("old/pay")
    public ResponseEntity<BigDecimal> queryOldSalesVolume(){
        BigDecimal pay = orderService.queryOldSalesVolume();
        return  ResponseEntity.ok(pay);
    }



    /**
     * 根据订单状态统计总数
     * @param status
     * @return
     */
    @GetMapping("status")
    public ResponseEntity<Integer> queryStatus(Integer status){
        int i = orderService.queryStatus(status);
        return ResponseEntity.ok(i);
    }

    /**
     * 根据订单状态查询订单信息
     * @param status
     * @return
     */
    @GetMapping("order/{status}")
    public ResponseEntity<List<Order>> queryOrderByStatus(@PathVariable("status") Integer status){
        List<Order> orders = orderService.queryOrderByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 统计库存为0的商品数量
     * @return
     */
    @GetMapping("stock/count")
    public ResponseEntity<Integer> querySkuStock(){
        Integer integer = itemClient.querySkuStock();
        return ResponseEntity.ok(integer);
    }

    /**
     * 查询所有库存为0的商品
     * @return
     */
    @GetMapping("sku/stock")
    public ResponseEntity<List<Sku>> querySkuByStock(){
        List<Sku> skus = itemClient.querySkuByStock();
        return ResponseEntity.ok(skus);
    }



    /**
     * 根据商品上下架状态统计商品总数
     * @param saleable
     * @return
     */
    @GetMapping("count/{saleable}")
    public ResponseEntity<Integer> querySpuCount(@PathVariable("saleable") boolean saleable){
        Integer i = itemClient.querySkuCount(saleable);
        return ResponseEntity.ok(i);
    }

    /**
     * 统计库存小于10的商品总数
     * @return
     */
    @GetMapping("count/stock")
    public ResponseEntity<Integer> queryStockByCount(){
        Integer integer = itemClient.queryStockByCount();
        return ResponseEntity.ok(integer);
    }

    /**
     * 查询库存小于10的商品详情
     * @return
     */
    @GetMapping("stock/sku")
    public ResponseEntity<List<Sku>> queryStockByCountSku(){
        List<Sku> skus = itemClient.queryStockByCountSku();
        return ResponseEntity.ok(skus);
    }

    /**
     * 统计sku商品总数
     * @return
     */
    @GetMapping("count/sku")
    public ResponseEntity<Integer> querySkuByCount(){
        Integer integer = itemClient.querySkuByCount();
        return ResponseEntity.ok(integer);
    }



    /**
     * 统计会员总数
     *
     * @return
     */
    @GetMapping("count/user")
    public ResponseEntity<Integer> queryUserCount() {
        Integer i = userClient.queryUserCount();
        return ResponseEntity.ok(i);
    }

    /**
     * 统计今天用户注册量
     *
     * @return
     */
    @GetMapping("count/day/user")
    public ResponseEntity<Integer> queryUserByCreatedDay() {
        Integer i = userClient.queryUserByCreatedDay();
        return ResponseEntity.ok(i);
    }

    /**
     * 统计昨天注册的用户数量
     * @return
     */
    @GetMapping("count/old/user")
    public ResponseEntity<Integer> queryUserByCreatedOld(){
        Integer i = userClient.queryUserByCreatedOld();
        return ResponseEntity.ok(i);
    }

    /**
     * 本月注册的用户数量
     * @return
     */
    @GetMapping("count/month/user")
    public ResponseEntity<Integer> queryUserByCreadMonth(){
        Integer i = userClient.queryUserByCreadMonth();
        return ResponseEntity.ok(i);
    }



    /**
     * 统计本月订单总数
     * @return
     */
    @GetMapping("order/create/month")
    public ResponseEntity<Integer> queryOrderByCreatTime(@RequestParam(value = "startTime", required = false) String startTime, @RequestParam(value = "endTime", required = false) String endTime){
        Integer i = orderService.queryOrderByCreatedMonth(startTime, endTime);
        return ResponseEntity.ok(i);
    }

    /**
     * 统计本周订单总数
     * @return
     */
    @GetMapping("order/create/week")
    public ResponseEntity<Integer> queryOrderByCreateWeek(){
        Integer i = orderService.queryOrderByCreateWeek();
        return ResponseEntity.ok(i);
    }

    /**
     * 统计本月销售总额
     * @return
     */
    @GetMapping("order/sales/volume/month")
    public ResponseEntity<BigDecimal> queryOrderByCreatedMonthSalesVolume(@RequestParam(value = "startTime", required = false) String startTime,@RequestParam(value = "endTime", required = false) String endTime){
        BigDecimal d = orderService.queryOrderByCreatedMonthSalesVolume(startTime, endTime);
        return ResponseEntity.ok(d);
    }

    /**
     * 统计本周销售总额
     * @return
     */
    @GetMapping("order/sales/volume/week")
    public ResponseEntity<BigDecimal> queryOrderByCreatedWeekSalesVolume(){
        BigDecimal d = orderService.queryOrderByCreatedWeekSalesVolume();
        return ResponseEntity.ok(d);
    }

    /**
     * 计算本月和上月销量的百分比
     * @return
     */
    @GetMapping("order/sales/percentage")
    public ResponseEntity<String> CalculateThePercentageOfMonthlyAndMonthlySales(@RequestParam(value = "startTime", required = false) String startTime,@RequestParam(value = "endTime", required = false) String endTime){
        String s = orderService.CalculateThePercentageOfMonthlyAndMonthlySales(startTime, endTime);
        return ResponseEntity.ok(s);
    }

    /**
     * 计算本月和上月订单总数的百分比
     * @return
     */
    @GetMapping("order/sales/count")
    public ResponseEntity<String> OrderCountOfMonthlyAndMonthly(@RequestParam(value = "startTime", required = false) String startTime,@RequestParam(value = "endTime", required = false) String endTime){
        String s = orderService.OrderCountOfMonthlyAndMonthly();
        return ResponseEntity.ok(s);
    }

    /**
     * 计算上周与本周销售总额的百分比
     * @return
     */
    @GetMapping("order/sales/percentage/week")
    public ResponseEntity<String> calculateLastWeekAndThisWeekSalesPercentage(){
        String s = orderService.calculateLastWeekAndThisWeekSalesPercentage();
        return ResponseEntity.ok(s);
    }

    /**
     * 计算上周与本周订单数量的百分比
     * @return
     */
    @GetMapping("order/sales/count/week")
    public  ResponseEntity<String> OrderCountOfWeekAndLastWeek(){
        String s = orderService.OrderCountOfWeekAndLastWeek();
        return ResponseEntity.ok(s);
    }

}
