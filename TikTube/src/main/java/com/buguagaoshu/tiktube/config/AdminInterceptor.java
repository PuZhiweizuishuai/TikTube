package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.enums.RoleTypeEnum;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 17:05
 */
@Service
@Slf4j
public class AdminInterceptor implements HandlerInterceptor {
    private final IpUtil ipUtil;

    @Autowired
    public AdminInterceptor(IpUtil ipUtil) {
        this.ipUtil = ipUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Claims claims = JwtUtil.getUser(request);
        if (claims == null) {
            log.warn("访问 ip 为 {} 访问管理员接口被拦截", ipUtil.getIpAddr(request));
            return false;
        }
        if (RoleTypeEnum.ADMIN.getRole().equals(claims.get("authorities"))) {
            return true;
        }
        log.warn("用户id为 {} 的用户访问管理员接口被拦截，访问 ip 为 {}", claims.getId(), ipUtil.getIpAddr(request));
        return false;
    }
}
