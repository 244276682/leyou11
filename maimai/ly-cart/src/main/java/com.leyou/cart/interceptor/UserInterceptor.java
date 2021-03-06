package com.leyou.cart.interceptor;


import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> tl=new ThreadLocal<>();

    public UserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties=jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
       //获取cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            //解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //传递user
            tl.set(user);

            //放行
            return true;
        } catch (Exception e) {
            log.error("【购物车服务】解析用户信息失败",e);
        }

        return false;
    }

    //视图渲染完执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //用完数据一定要清空
        tl.remove();
    }
    public static UserInfo getUser(){
        return tl.get();
    }
}
