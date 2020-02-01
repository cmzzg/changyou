package cn.changyou.user.service;

import cn.changyou.common.utils.NumberUtils;
import cn.changyou.user.mapper.UserMapper;
import cn.changyou.user.pojo.User;
import cn.changyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xgl
 * @create 2019-12-27 0:35
 */
@Service
public class UserService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private UserMapper userMapper;
    private Logger log = LoggerFactory.getLogger(UserService.class);
    static final String KEY_PREFIX = "user:code:phone:";
    static final Logger logger = LoggerFactory.getLogger(UserService.class);
    /**
     * 实现用户数据的校验，主要包括对：手机号、用户名的唯一性校验。
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if (type == 1){
            user.setUsername(data);
        }else if (type == 2){
            user.setPhone(data);
        }else {
            return null;
        }
        return userMapper.selectCount(user) == 0;
    }

    public Boolean sendCode(String phone) {

        //生成验证码
        String code = NumberUtils.generateCode(6);
        log.info("生成的验证码为{}",code);
        try {
            //发送到MQ
            Map<String,String> map = new HashMap<>();
            map.put("phone",phone);
            map.put("code",code);
            amqpTemplate.convertAndSend("changyou.sms.exchange","code.sms",map);
            log.info("发送到mq成功");
            //放入Redis
            redisTemplate.opsForValue().set(KEY_PREFIX + phone,code,5, TimeUnit.MINUTES);
            log.info("发送到Redis成功,有效期5分钟");

            return true;
        } catch (AmqpException e) {
            e.printStackTrace();
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }

    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    public Boolean register(User user, String code) {
        //0.查询Redis中的验证码

        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        log.info("查询redis中的验证码,验证码为{}",redisCode);
        //1.校验验证码
        if (!StringUtils.equals(code,redisCode)){
            log.info("此时,校验不通过,用户输入的code为{},redis当中的code为{}",code,redisCode);
            return false;
        }
        log.info("验证码校验通过");
        //2.生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //3.加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        //4.新增用户
        user.setId(null);
        user.setCreated(new Date());
        userMapper.insertSelective(user);
        log.info("用户新增成功");
        //5.删除Redis中的验证码
        redisTemplate.delete(KEY_PREFIX + user.getPhone());
        return true;
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        User one = userMapper.selectOne(user);
        log.info("通过传入的用户名进行查询,查询出来的结果为{}",one);
        //判断user是否为空
        if (one == null){
            log.info("此时,么有查询到用户,及未注册");
            return null;
        }
        //获取盐
        String salt = one.getSalt();
        log.info("获取的盐为{}",salt);
        //对用户输入的密码加盐加密
        password = CodecUtils.md5Hex(password,one.getSalt());
        log.info("对输入进来的密码进行加密,加密后的结果为{}",password);
        //和数据库中的密码进行比较
        if (StringUtils.equals(password,one.getPassword())) {
            log.info("此时,比较成功,及登录成功");
            return one;
        }
        return null;
    }

    /**
     * 第三方登录
     * @param user
     * @return
     */
    public User thirdLogin(User user){
        return userMapper.selectOne(user);
    }
}
