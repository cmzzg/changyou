package cn.changyou.order.controller;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.order.pojo.Order;
import cn.changyou.order.pojo.OrderStatus;
import cn.changyou.order.service.PayService;
import cn.changyou.order.service.OrderService;
import cn.changyou.utils.PayHelper;
import cn.changyou.utils.PayState;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("order")
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private PayService payService;

    private Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * 创建订单
     *
     * @param order 订单对象
     * @return 订单编号
     */
    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号", notes = "创建订单")
    @ApiImplicitParam(name = "order", required = true, value = "订单的json对象,包含订单条目和物流信息")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid Order order) {
        log.info("准备开始创建订单,订单信息为{}",order);
        Long id = this.orderService.createOrder(order);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    /**
     * 根据订单编号查询订单
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation(value = "根据订单编号查询订单，返回订单对象", notes = "查询订单")
    @ApiImplicitParam(name = "id", required = true, value = "订单的编号")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        Order order = this.orderService.queryById(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(order);
    }
    @GetMapping("state/{id}")
    public ResponseEntity<OrderStatus> queryOrderStatusById(@PathVariable("id") Long id){
        OrderStatus status = orderService.queryOrderStatusById(id);
        if (status == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    /**
     * 分页查询当前用户订单
     *
     * @param status 订单状态
     * @return 分页订单数据
     */
    @GetMapping("list")
    @ApiOperation(value = "分页查询当前用户订单，并且可以根据订单状态过滤", notes = "分页查询当前用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页", defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小", defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "订单的分页结果"),
            @ApiResponse(code = 404, message = "没有查询到结果"),
            @ApiResponse(code = 500, message = "查询失败"),
    })
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<Order> result = this.orderService.queryUserOrderList(page, rows, status);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新订单状态
     *
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    @ApiOperation(value = "更新订单状态", notes = "更新订单状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", type = "Long"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })

    @ApiResponses({
            @ApiResponse(code = 204, message = "true：修改状态成功；false：修改状态失败"),
            @ApiResponse(code = 400, message = "请求参数有误"),
            @ApiResponse(code = 500, message = "查询失败")
    })
    public ResponseEntity<Boolean> updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        Boolean boo = this.orderService.updateStatus(id, status);
        if (boo == null) {
            // 返回400
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 返回204
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 生成微信付款链接
     *
     * @return
     */
    @PostMapping("wxpay")
    @ApiOperation(value = "生成微信扫码支付付款链接", notes = "生成付款链接")
    @ApiImplicitParam(name = "order", value = "订单信息", type = "Order")
    @ApiResponses({
            @ApiResponse(code = 200, message = "根据订单编号生成的微信支付地址"),
            @ApiResponse(code = 404, message = "生成链接失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<String> createWxPayUrl(@RequestBody Order order) {
        // 生成付款链接
        String url = this.payService.createWxPayUrl(order.getOrderId(),order.getTitle(),order.getActualPay().toString());
        log.info("生成付款链接,{},{},{},{}",order.getOrderId(),order.getTitle(),order.getActualPay(),url);
        if (StringUtils.isBlank(url)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(url);
    }

    /**
     * 查询微信付款状态
     *
     * @param orderId
     * @return 0, 状态查询失败 1,支付成功 2,支付失败
     */
    @GetMapping("wxstate/{id}")
    @ApiOperation(value = "查询扫码支付付款状态", notes = "查询付款状态")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "0, 未查询到支付信息 1,支付成功 2,支付失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId) {
        PayState payState = this.payService.queryWxOrder(orderId);
        log.info("订单{}的状态为{}",orderId,payState.getValue());
        return ResponseEntity.ok(payState.getValue());
    }

    /**
     * 创建阿里支付URl
     * @param order
     * @return
     */
    @PostMapping("alipay")
    @ApiOperation(value = "生成支付宝扫码支付付款链接", notes = "生成付款链接")
    @ApiImplicitParam(name = "order", value = "订单信息", type = "Order")
    @ApiResponses({
            @ApiResponse(code = 200, message = "根据订单编号生成的微信支付地址"),
            @ApiResponse(code = 404, message = "生成链接失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<String> aliPay(@RequestBody Order order){
        log.info("支付宝的参数{},标题为{}",order,order.getTitle());
        String url = payService.createAliPayUrl(order);
        //String url = aliPayHelper.createPayUrl(order);
        return ResponseEntity.ok(url);
    }

    /**
     * 查询阿里付款状态
     * @param orderId
     * @return
     */
    @GetMapping("alistate/{id}")
    @ApiOperation(value = "查询阿里扫码支付付款状态", notes = "查询付款状态")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "0, 未查询到支付信息 1,支付成功 2,支付失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<Integer> queryAliPayState(@PathVariable("id") Long orderId) {
        if (StringUtils.isEmpty(orderId.toString())) {
            return ResponseEntity.badRequest().build();
        }
        PayState payState = payService.queryAliPayState(orderId);
        if (payState == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payState.getValue());
    }
}
