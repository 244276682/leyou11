package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;


    public String login(String username, String password) {
        try{
        //判断用户名和密码
        User user = userClient.queryUserByUsernameAndPassword(username, password);
        //判断
        if(user==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //生成token
        String token=JwtUtils.generateToken(new UserInfo(user.getId(),username),properties.getPrivateKey(),properties.getExpire());
        return token;}
        catch (Exception e){
            log.error("【授权中心】生成token失败，用户名称：{}",username,e);
            throw  new LyException(ExceptionEnum.CREATE_TOKEN_ERROR);
        }
    }
}
