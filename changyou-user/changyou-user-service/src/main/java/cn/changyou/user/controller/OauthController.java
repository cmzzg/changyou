package cn.changyou.user.controller;

import cn.changyou.common.utils.CookieUtils;
import cn.changyou.user.config.JwtProperties;
import cn.changyou.user.service.OauthService;
import cn.hutool.core.util.StrUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 第三方登录 Controller
 * </p>
 *
 * @package: com.xkcoding.oauth.controller
 * @description: 第三方登录 Controller
 * @author: yangkai.shen
 * @date: Created in 2019-05-17 10:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: yangkai.shen
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OauthController {
    private final AuthRequestFactory factory;
    private final OauthService oauthService;
    private final JwtProperties jwtProperties;

    /**
     * 登录类型
     */
    @GetMapping
    public Map<String, String> loginType() {
        List<String> oauthList = factory.oauthList();
        return oauthList.stream().collect(Collectors.toMap(oauth -> oauth.toLowerCase() + "登录", oauth -> "http://changyou.xgl6.cn/oauth/login/" + oauth.toLowerCase()));
    }
    /**
     * 登录
     *
     * @param oauthType 第三方登录类型
     * @param response  response
     * @throws IOException
     */
    @RequestMapping("/login/{oauthType}")
    public void renderAuth(@PathVariable String oauthType, HttpServletResponse response) throws IOException {
        //AuthRequest authRequest = factory.get(getAuthSource(oauthType));
        AuthDefaultSource source = getAuthSource(oauthType);

        AuthRequest authRequest = factory.get(source.toString());
        response.sendRedirect(authRequest.authorize(oauthType + "::" + AuthStateUtils.createState()));
    }


    /**
     * 登录成功后的回调
     *
     * @param oauthType 第三方登录类型
     * @param callback  携带返回的信息
     * @return 登录成功后的信息
     */
    @RequestMapping("/callback/{oauthType}")
    public void login(@PathVariable String oauthType, AuthCallback callback, HttpServletRequest request,HttpServletResponse response) throws IOException {
        String token = oauthService.thirdLogin(oauthType, callback);
        if (StringUtils.isBlank(token)){
            response.sendRedirect("http://changyou.xgl6.cn/login.html");
            return;
        }
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire() * 60);
        response.sendRedirect("http://changyou.xgl6.cn");
    }

    public static AuthDefaultSource getAuthSource(String type) {
        if (StrUtil.isNotBlank(type)) {
            return AuthDefaultSource.valueOf(type.toUpperCase());
        } else {
            throw new RuntimeException("不支持的类型");
        }
    }
}
