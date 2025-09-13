package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {
    @Autowired
    JwtProperties jwtProperties;

    /**
     * 请求到Controller资源之前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*if(handler instanceof HandlerMethod){
            return true;
        }*/

        //请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //解析令牌并把用户id放入ThreadLocal中
        try {
            log.info("校验令牌:{}",token);

            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("用户id:{}",userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }


        //return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
