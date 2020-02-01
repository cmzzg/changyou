
package cn.changyou.order.service;

import cn.changyou.config.AliPayProperties;
import cn.changyou.config.PayConfig;
import cn.changyou.order.pojo.Order;
import cn.changyou.utils.PayState;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.github.wxpay.sdk.WXPay;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xgl
 * @create 2020-01-07 19:14
 */

@Service
public class PayService {
    private WXPay wxPay;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AliPayProperties aliPayProperties;
    @Autowired
    private OrderService orderService;

    private Logger log = LoggerFactory.getLogger(PayService.class);

    public PayService(PayConfig payConfig) {
        // 真实开发时
        wxPay = new WXPay(payConfig);
        // 测试时
        // wxPay = new WXPay(payConfig, WXPayConstants.SignType.MD5, true);
    }

    public void getApiConfig() {
        AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder()
                .setAppId(aliPayProperties.getAppId())
                .setAliPayPublicKey(aliPayProperties.getPublicKey())
                .setCharset("UTF-8")
                .setPrivateKey(aliPayProperties.getPrivateKey())
                .setServiceUrl(aliPayProperties.getServerUrl())
                .setSignType("RSA2")
                .build(); // 普通公钥方式
        AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
    }

    /**
     * 创建阿里支付URl
     *
     * @param order
     * @return
     */
    public String createAliPayUrl(Order order) {
        //初始化阿里胚子
        getApiConfig();
        String key = "cy.pay.ali.url." + order.getOrderId();
        try {
            String url = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            log.error("查询缓存付款链接异常,订单编号：{}", order.getOrderId(), e);
        }
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(order.getTitle());
        String price = BigDecimal.valueOf(Long.valueOf(order.getActualPay())).divide(new BigDecimal(100)).toString();
        model.setTotalAmount("0.1");
        model.setStoreId("666");
        model.setTimeoutExpress("5m");
        model.setOutTradeNo(order.getOrderId().toString());
        String notifyUrl = "http://changyou.xgl6.cn/paysuccess.html";
        try {
            String body = AliPayApi.tradePrecreatePayToResponse(model, notifyUrl).getBody();
            System.out.println(body);
            JSONObject jsonObject = JSONObject.parseObject(body);
            String url = jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");
            this.redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
            return url;
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询阿里付款状态
     *
     * @param orderId
     * @return
     */
    public PayState queryAliPayState(Long orderId) {
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();

            model.setOutTradeNo(orderId.toString());

            String body = AliPayApi.tradeQueryToResponse(model).getBody();
            if (body == null){
                //未查询到付款结果,认为是未付款
                return PayState.NOT_PAY;
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            log.info("支付返回的订单结果为{}", body);
            log.info("序列化结果{}",jsonObject);
            String result = jsonObject.getJSONObject("alipay_trade_query_response").getString("trade_status");
            log.info("支付宝状态:{}",result);
            if (StringUtils.equals(result,"TRADE_SUCCESS")){
                //此时,交易支付成功
                this.orderService.updateStatus(orderId, 2);
                return PayState.SUCCESS;
            }
            if (StringUtils.equals(result,"WAIT_BUYER_PAY")){
                //此时,交易创建，等待买家付款
                return PayState.NOT_PAY;
            }
            if (StringUtils.equals(result,"TRADE_CLOSED")){
                //此时,未付款交易超时关闭，或支付完成后全额退款
                return PayState.FAIL;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建微信支付URL
     *
     * @param orderId
     * @param title
     * @param money
     * @return
     */
    public String createWxPayUrl(Long orderId, String title, String money) {
        String key = "cy.pay.url." + orderId;
        try {
            String url = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            log.error("查询缓存付款链接异常,订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", title);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", "1");
            //调用微信支付的终端IP（乐优商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址，付款成功后的接口
            data.put("notify_url", "http://test.leyou.com/wxpay/notify");
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            data.put("product_id", "1234567");

            Map<String, String> result = this.wxPay.unifiedOrder(data);

            if ("SUCCESS".equals(result.get("return_code"))) {
                String url = result.get("code_url");
                // 将付款地址缓存，时间为10分钟
                try {
                    this.redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("缓存付款链接异常,订单编号：{}", orderId, e);
                }
                return url;
            } else {
                log.error("创建预交易订单失败，错误信息：{}", result.get("return_msg"));
                return null;
            }
        } catch (Exception e) {
            log.error("创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 查询微信订单状态
     *
     * @param orderId
     * @return
     */
    public PayState queryWxOrder(Long orderId) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = this.wxPay.orderQuery(data);
            if (result == null) {
                // 未查询到结果，认为是未付款
                return PayState.NOT_PAY;
            }
            String state = result.get("trade_state");
            log.info("state:{}",state);
            if ("SUCCESS".equals(state)) {
                // success，则认为付款成功

                // 修改订单状态
                this.orderService.updateStatus(orderId, 2);
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                // 未付款或正在付款，都认为是未付款
                return PayState.NOT_PAY;
            } else if (StringUtils.equals("CLOSED", state)){
                // 其它状态认为是付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            log.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
        return PayState.NOT_PAY;
    }
}

