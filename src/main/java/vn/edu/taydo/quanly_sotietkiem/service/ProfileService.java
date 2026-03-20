package vn.edu.taydo.quanly_sotietkiem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    /**
     * Lấy KhachHang từ JWT cookie trong request.
     * Trả về null nếu chưa đăng nhập hoặc không tìm thấy.
     */
    public KhachHang getKhachHangFromRequest(HttpServletRequest request) {
        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        if (claims == null) return null;

        String role = (String) claims.get("role");
        if (!"K_H".equals(role)) return null;

        String khachHangId = (String) claims.get("userId");
        if (khachHangId == null) return null;

        Optional<KhachHang> opt = khachHangRepository.findById(khachHangId);
        return opt.orElse(null);
    }
}