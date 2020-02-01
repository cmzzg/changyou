package cn.changyou.user.service;


import cn.changyou.common.pojo.UserInfo;
import cn.changyou.common.utils.JwtUtils;
import cn.changyou.user.config.JwtProperties;
import cn.changyou.user.controller.OauthController;
import cn.changyou.user.interceptor.LoginInterceptor;
import cn.changyou.user.mapper.UserMapper;
import cn.changyou.user.pojo.User;
import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xgl
 * @create 2020-01-12 17:42
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class OauthService {
    private final AuthRequestFactory factory;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    @Autowired
    private UserMapper userMapper;
    public String thirdLogin(String oauthType, AuthCallback callback){
        AuthRequest authRequest = factory.get(OauthController.getAuthSource(oauthType).toString());
        AuthResponse response = authRequest.login(callback);
        log.info("【response】= {}", JSONUtil.toJsonStr(response));
        String s = JSONUtil.toJsonStr(response.getData());
        log.info("data = {}",s);
        User user = new User();
        String uuid = JSONUtil.parseObj(s).get("uuid").toString();
        if (StringUtils.equals(oauthType,"qq")){
            user.setQq(uuid);
        }
        if (StringUtils.equals(oauthType,"github")){
            user.setGithub(uuid);
        }
        if (StringUtils.equals(oauthType,"alipay")){
            user.setAlipay(uuid);
        }
        if (StringUtils.equals(oauthType,"weibo")){
            user.setWeibo(uuid);
        }
        User resultUser = userService.thirdLogin(user);
        UserInfo userInfo = new UserInfo();
        if (resultUser == null){
            //此时,该用户未与任何第三方平台绑定
            UserInfo info = LoginInterceptor.getUserInfo();
            log.info("登录拦截器当中的用户{}",info);
            User user1 = userMapper.selectByPrimaryKey(info.getId());
            log.info("此时,查询出来的用户信息为{}",user1);
            if (user1 == null){
                log.info("此时,用户为未注册状态,不生成token");
                return null;
            }
            if (StringUtils.equals(oauthType,"qq")){
                log.info("此时,用户进入的是qq绑定");
                user1.setQq(uuid);
            }
            if (StringUtils.equals(oauthType,"github")){
                user1.setGithub(uuid);
            }
            if (StringUtils.equals(oauthType,"alipay")){
                user1.setAlipay(uuid);
            }
            if (StringUtils.equals(oauthType,"weibo")){
                user1.setWeibo(uuid);
            }
            userMapper.updateByPrimaryKeySelective(user1);
            log.info("将设置的值插入数据库");
            userInfo.setId(user1.getId());
            userInfo.setUsername(user1.getUsername());
        }else {
            userInfo.setId(resultUser.getId());
            userInfo.setUsername(resultUser.getUsername());
        }
        try {
            return JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
