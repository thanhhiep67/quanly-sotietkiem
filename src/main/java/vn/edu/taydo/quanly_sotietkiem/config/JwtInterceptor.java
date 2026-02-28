package vn.edu.taydo.quanly_sotietkiem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Lấy token từ cookie
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isEmpty()) {
            response.sendRedirect("/login");
            return false;
        }

        try {
            Claims claims = JwtUtil.validateToken(token);
            request.setAttribute("claims", claims);
            return true;
        } catch (JwtException e) {
            response.sendRedirect("/login");
            return false;
        }
    }


}

