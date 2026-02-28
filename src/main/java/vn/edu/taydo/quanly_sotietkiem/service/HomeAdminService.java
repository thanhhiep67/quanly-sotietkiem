package vn.edu.taydo.quanly_sotietkiem.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.NhanVien;
import vn.edu.taydo.quanly_sotietkiem.repository.NhanVienRepository;

@Service
public class HomeAdminService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    public String getTenNhanVien(HttpServletRequest request) {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) return null;

        Claims claims = JwtUtil.extractClaims(token);

        return claims.get("userId", String.class);
    }
}
