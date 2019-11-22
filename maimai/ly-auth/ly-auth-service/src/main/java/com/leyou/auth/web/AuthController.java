package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties properties;

    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username")String username, @RequestParam("password")String password,
                                      HttpServletRequest request, HttpServletResponse response){
        //登录
        String token=authService.login(username,password);
        if (StringUtils.isBlank(token)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //2.将token写入cookie，并指定httpOnly为true，防止通过js获取和修改
        CookieUtils.setCookie(request,response,properties.getCookieName(),token);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查用户登录状态
     * @param token
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token, HttpServletRequest request, HttpServletResponse response){
        if(StringUtils.isBlank(token)){
            //如果没有token 返回403
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        try {
            UserInfo info=JwtUtils.getInfoFromToken(token,properties.getPublicKey());
            //刷新token，重新生成token
            String newToken = JwtUtils.generateToken(info, properties.getPrivateKey(), properties.getExpire());
           //写入cookie
            CookieUtils.setCookie(request,response,properties.getCookieName(),token);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            //如果token已过期，或者token被修改
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
