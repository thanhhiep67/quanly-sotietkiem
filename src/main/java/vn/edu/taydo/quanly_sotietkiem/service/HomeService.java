package vn.edu.taydo.quanly_sotietkiem.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;

import java.util.Optional;

@Service
public class HomeService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    public String getTenKhachHang(HttpServletRequest request) {
        String token = null;

        // Lấy cookie "token"
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) return null;

        // Giải mã JWT
        Claims claims = JwtUtil.extractClaims(token);
        String userId = claims.get("userId", String.class);

        // Tìm khách hàng theo userId
        return khachHangRepository.findById(userId)
                .map(KhachHang::getHoten)
                .orElse(null);
    }
}
