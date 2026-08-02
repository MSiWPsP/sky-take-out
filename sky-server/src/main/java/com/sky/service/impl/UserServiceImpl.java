package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    //微信服务接口地址
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用getOpenid方法获取openid
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否为空,如果为空,表示登录失败,抛出业务异常
        if (openid == null || openid.isEmpty()){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //如果不为空,则判断当前用户是否为当前系统的新用户(即判断用户openid是否在用户表里)
        User user = userMapper.getByOpenId(openid);
        if (user == null){
            //如果是新用户,则自动完成注册(自己封装一个user对象,把它保存到用户表里)
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //返回一个user对象
        return user;
    }

    /**
     * 调用微信接口服务,获得当前微信用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
