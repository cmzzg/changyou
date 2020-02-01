package cn.changyou.user.interceptor;


import cn.changyou.common.pojo.UserInfo;
import cn.changyou.common.utils.CookieUtils;
import cn.changyou.common.utils.JwtUtils;
import cn.changyou.user.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xgl
 * @create 2020-01-05 18:53
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtProperties jwtProperties;
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取cookie中的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        System.out.println("token为:" + token);
        //2.解析token,获取用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        System.out.println("userInfo为:" + userInfo);
        if (userInfo == null){
            return false;
        }
        //3.放入ThreadLocal
        THREAD_LOCAL.set(userInfo);
        return true;
    }
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空线程的局部变量,因为使用的是tomcat的线程池,线程不会结束,也就不会释放线程的局部变量
        THREAD_LOCAL.remove();
    }
}
