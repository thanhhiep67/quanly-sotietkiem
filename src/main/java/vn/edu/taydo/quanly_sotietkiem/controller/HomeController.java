package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.service.HomeService;

import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        // Lấy claims từ JWT cookie
        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        if (claims == null) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            return "redirect:/login";
        }

        String role = (String) claims.get("role");
        String khachHangId = (String) claims.get("userId");

        // Chỉ cho phép khách hàng vào trang này
        if (!"K_H".equals(role)) {
            return "redirect:/login?error=unauthorized";
        }

        // Lấy tên khách hàng
        String tenKhachHang = homeService.getTenKhachHang(request);
        if (tenKhachHang != null) {
            model.addAttribute("tenKhachHang", tenKhachHang);
            model.addAttribute("role", role);

            // Lấy tổng tài sản tiết kiệm
            double tongTaiSan = homeService.getTongTaiSanTietKiem(khachHangId);
            model.addAttribute("tongTaiSan", tongTaiSan);

            // Lấy số lượng sổ tiết kiệm
            int soLuongSo = homeService.getSoLuongSoTietKiem(khachHangId);
            model.addAttribute("soLuongSo", soLuongSo);

        } else {
            model.addAttribute("errorMessage", "Không tìm thấy khách hàng hoặc chưa đăng nhập!");
        }

        return "qlstk/client/index";
    }
}
