package vn.edu.taydo.quanly_sotietkiem.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (token != null) {
            Map<String, Object> claims = JwtUtil.extractClaims(token);
            String role = (String) claims.get("role");

            // Nếu vào /admin mà không phải ADMIN thì chặn
            if (request.getRequestURI().startsWith("/admin") && !"ADMIN".equals(role)) {
                response.sendRedirect("/login?error=unauthorized");
                return false;
            }

            // Nếu vào /client mà không phải K_H thì chặn
            if (request.getRequestURI().startsWith("/client") && !"K_H".equals(role)) {
                response.sendRedirect("/login?error=unauthorized");
                return false;
            }
        } else {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}

