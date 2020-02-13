package cn.changyou.order.service;

import cn.changyou.common.pojo.PageResult;
import cn.changyou.common.pojo.UserInfo;
import cn.changyou.common.utils.IdWorker;
import cn.changyou.order.interceptor.LoginInterceptor;
import cn.changyou.order.mapper.OrderDetailMapper;
import cn.changyou.order.mapper.OrderMapper;
import cn.changyou.order.mapper.OrderStatusMapper;
import cn.changyou.order.pojo.Order;
import cn.changyou.order.pojo.OrderDetail;
import cn.changyou.order.pojo.OrderStatus;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);


    @Transactional
    public Long createOrder(Order order) {
        // 生成orderId
        long orderId = idWorker.nextId();
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        // 初始化数据
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        // 保存数据
        this.orderMapper.insertSelective(order);

        // 保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(1);// 初始状态为未付款

        this.statusMapper.insertSelective(orderStatus);

        // 订单详情中添加orderId
        order.getOrderDetails().forEach(od -> od.setOrderId(orderId));
        // 保存订单详情,使用批量插入功能
        this.detailMapper.insertList(order.getOrderDetails());

        logger.debug("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());

        return orderId;
    }

    public Order queryById(Long id) {
        // 查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);

        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = this.detailMapper.select(detail);
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus status = this.statusMapper.selectByPrimaryKey(order.getOrderId());
        order.setStatus(status.getStatus());
        return order;
    }

    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 获取登录用户
            UserInfo user = LoginInterceptor.getLoginUser();
            // 创建查询条件
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(user.getId(), status);

            return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }

    @Transactional
    public Boolean updateStatus(Long id, Integer status) {
        OrderStatus record = new OrderStatus();
        record.setOrderId(id);
        record.setStatus(status);
        // 根据状态判断要修改的时间
        switch (status) {
            case 2:
                record.setPaymentTime(new Date());// 付款
                break;
            case 3:
                record.setConsignTime(new Date());// 发货
                break;
            case 4:
                record.setEndTime(new Date());// 确认收获，订单结束
                break;
            case 5:
                record.setCloseTime(new Date());// 交易失败，订单关闭
                break;
            case 6:
                record.setCommentTime(new Date());// 评价时间
                break;
            default:
                return null;
        }
        int count = this.statusMapper.updateByPrimaryKeySelective(record);
        return count == 1;
    }

    public OrderStatus queryOrderStatusById(Long id) {
        return statusMapper.selectByPrimaryKey(id);
    }

    public void updateOrderStatusById(Long orderId) {
        statusMapper.updateOrderStatusById(orderId);
    }

    /**
     * 统计当天订单总数
     *
     * @return 数量
     */
    public int queryOrderCount() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Date d = new Date();
        String str = sdf.format(d);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String str1 = sdf1.format(d);
        int i = orderMapper.orderCount(str, str1);
        return i;
    }

    /**
     * 查询一段时间内的订单的实付金额,并进行计算
     *
     * @param str  开始时间
     * @param str1 结束时间
     * @return
     */
    private BigDecimal count(String str, String str1) {
        List<BigDecimal> list = orderMapper.orderSalesVolume(str, str1);
        BigDecimal volumn = new BigDecimal("0");
        for (BigDecimal decimal : list) {
            if (volumn.equals(0)) {
                volumn = decimal;
            }
            volumn = volumn.add(decimal);
        }
        BigDecimal divide = volumn.divide(new BigDecimal(100));
        return divide;
    }

    /**
     * 根据状态查询订单总数
     *
     * @param status
     * @return
     */
    public int queryStatus(Integer status) {
        OrderStatus os = new OrderStatus();
        os.setStatus(status);
        int i = statusMapper.selectCount(os);
        return i;
    }

    /**
     * 根据状态查询订单详情
     *
     * @param status
     * @return
     */
    public List<Order> queryOrderByStatus(Integer status) {
        List<Long> orderIds = statusMapper.queryStatus(status);
        List<Order> orders = new ArrayList<>();
        for (Long orderId : orderIds) {
            Order order = orderMapper.selectByPrimaryKey(orderId);
            orders.add(order);
        }
        return orders;
    }

    /**
     * 根据订单创建时间查询订单总数
     *
     * @param str
     * @param str1
     * @return
     */
    private Integer queryOrderByCreatTime(String str, String str1) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andBetween("createTime", str, str1);
        Integer i = orderMapper.selectCountByExample(example);
        return i;
    }

    /**
     * 查询本月订单总数
     *
     * @return
     */
    public Integer queryOrderByCreatedMonth(String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            String monthStartDay = getMonthStartDay();
            String monthEndDay = getMonthEndDay();
            Integer i = queryOrderByCreatTime(monthStartDay, monthEndDay);
            return i;
        } else {
            String sf = "yyyy-MM-dd HH:mm:ss";
            String startFormat = timeStamp2Date(startTime, sf);
            String endFormat = timeStamp2Date(endTime, sf);
            Integer i = queryOrderByCreatTime(startFormat, endFormat);
            return i;
        }
    }

    /**
     * 查询本周订单总数
     *
     * @return
     */
    public Integer queryOrderByCreateWeek() {
        String weekStartDay = getWeekStartDay();
        String weekEndDay = getWeekEndDay();
        Integer integer = queryOrderByCreatTime(weekStartDay, weekEndDay);
        return integer;
    }

    /**
     * 查询本月订单销量
     *
     * @return
     */
    public BigDecimal queryOrderByCreatedMonthSalesVolume(String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            String monthStartDay = getMonthStartDay();
            String monthEndDay = getMonthEndDay();
            BigDecimal count = count(monthStartDay, monthEndDay);
            return count;
        }
        BigDecimal count = queryOrderByCreatedSelectSalesVolume(startTime, endTime);
        return count;
    }

    /**
     * 查询本周订单销量
     *
     * @return
     */
    public BigDecimal queryOrderByCreatedWeekSalesVolume() {
        String weekStartDay = getWeekStartDay();
        String weekEndDay = getWeekEndDay();
        BigDecimal count = count(weekStartDay, weekEndDay);
        return count;
    }

    //计算销售额百分比格式化
    private DecimalFormat df = new DecimalFormat("0.00%");

    /**
     * 本月和上月时间调用
     *
     * @return
     */
    public String CalculateThePercentageOfMonthlyAndMonthlySales(String startTime, String endTime) {
        String lastMonthStartDay = getLastMonthStartDay();
        String lastMonthEndDay = getLastMonthEndDay();
        BigDecimal count = count(lastMonthStartDay, lastMonthEndDay);
        String monthStartDay = getMonthStartDay();
        String monthEndDay = getMonthEndDay();
        BigDecimal count1 = count(monthStartDay, monthEndDay);
        String s = CalculateThePercentage(count, count1);
        return s;
    }

    /**
     * 计算一定时间内销量的百分比
     *
     * @param count
     * @param count1
     * @return
     */
    private String CalculateThePercentage(BigDecimal count, BigDecimal count1) {
        if (count.intValue() == 0) {
            return count1.toString();
        }
        BigDecimal sales = count1.subtract(count);
        BigDecimal divide = sales.divide(count, 2, BigDecimal.ROUND_HALF_UP);
        String s = df.format(divide);
        return s;
    }

    /**
     * 订单总数百分比格式化
     */
    NumberFormat numberFormat = NumberFormat.getInstance();

    /**
     * 计算本月和上月订单总数的百分比
     *
     * @return
     */
    public String OrderCountOfMonthlyAndMonthly() {
        String lastMonthStartDay = getLastMonthStartDay();
        String lastMonthEndDay = getLastMonthEndDay();
        Integer i = queryOrderByCreatTime(lastMonthStartDay, lastMonthEndDay);
        String monthStartDay = getMonthStartDay();
        String monthEndDay = getMonthEndDay();
        Integer i1 = queryOrderByCreatTime(monthStartDay, monthEndDay);
        String s = OrderCount(i, i1);
        return s;
    }

    /**
     * 上周与本周时间调用
     *
     * @return
     */
    public String calculateLastWeekAndThisWeekSalesPercentage() {
        String lastWeekMonday = getLastWeekMonday();
        String endDayOfLastWeek = getEndDayOfLastWeek();
        BigDecimal count = count(lastWeekMonday, endDayOfLastWeek);
        String weekStartDay = getWeekStartDay();
        String weekEndDay = getWeekEndDay();
        BigDecimal count1 = count(weekStartDay, weekEndDay);
        String s = CalculateThePercentage(count, count1);
        return s;
    }

    /**
     * 计算本周和上周订单总数的百分比
     *
     * @return
     */
    public String OrderCountOfWeekAndLastWeek() {
        String lastWeekMonday = getLastWeekMonday();
        String endDayOfLastWeek = getEndDayOfLastWeek();
        Integer i = queryOrderByCreatTime(lastWeekMonday, endDayOfLastWeek);
        String weekStartDay = getWeekStartDay();
        String weekEndDay = getWeekEndDay();
        Integer i1 = queryOrderByCreatTime(weekStartDay, weekEndDay);
        String s = OrderCount(i, i1);
        return s;
    }

    /**
     * 计算一定时间内订单的百分比
     *
     * @param i
     * @param i1
     * @return
     */
    public String OrderCount(Integer i, Integer i1) {
        numberFormat.setMaximumFractionDigits(2);
        if (i == 0) {
            return i1.toString();
        }
        Integer i2 = 0;
        if (i > i1) {
            i2 = i - i1;
        } else {
            i2 = i1 - i;
        }
        String result = numberFormat.format((double) i2 / (double) i * 100);
        if (i > i1) {
            return "-" + result + "%";
        }
        return result + "%";
    }


    /**
     * 根据用户选择的日期进行销量的查询计算
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public BigDecimal queryOrderByCreatedSelectSalesVolume(String startTime, String endTime) {
        String sf = "yyyy-MM-dd HH:mm:ss";
        String startFormat = timeStamp2Date(startTime, sf);
        String endFormat = timeStamp2Date(endTime, sf);
        BigDecimal count = count(startFormat, endFormat);
        return count;
    }

    /**
     * 将时间戳转换成字符串
     *
     * @param seconds
     * @param format
     * @return
     */
    public String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }


    /**
     * 以下是获取时间的方法
     */

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 统计当天日期
     *
     * @return
     */
    public BigDecimal querySalesVolume() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Date d = new Date();
        String str = sdf.format(d);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String str1 = sdf1.format(d);
        BigDecimal divide = count(str, str1);
        return divide;
    }

    /**
     * 获取前一天的日期
     *
     * @return
     */
    public BigDecimal queryOldSalesVolume() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String str = sdf.format(calendar.getTime());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String str1 = sdf1.format(calendar.getTime());
        BigDecimal divide = count(str, str1);
        return divide;
    }

    /**
     * 获取上个月开始的第一天
     *
     * @return
     */
    public String getLastMonthStartDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        String day = sdf.format(c.getTime());
        return day;
    }

    /**
     * 获取上个月最后一天
     *
     * @return
     */
    public String getLastMonthEndDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 31);
        String day = sdf.format(c.getTime());
        return day;
    }

    /**
     * 获取本月开始的第一天
     *
     * @return
     */
    public String getMonthStartDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        String day = sdf.format(c.getTime());
        return day;
    }

    /**
     * 获取本月最后一天
     *
     * @return
     */
    public String getMonthEndDay() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String day = sdf.format(ca.getTime());
        return day;
    }

    /**
     * 获取上周第一天
     *
     * @return
     */
    public String getLastWeekMonday() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek - 7);
        return sdf.format(cal.getTime());
    }

    /**
     * 获取上周最后一天
     *
     * @return
     */
    public String getEndDayOfLastWeek() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return sdf.format(c.getTime());
    }

    /**
     * 获取本周第一天
     *
     * @return
     */
    public String getWeekStartDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return sdf.format(c.getTime());
    }

    /**
     * 获取本周最后一天
     *
     * @return
     */
    public String getWeekEndDay() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return sdf.format(c.getTime());
    }


}