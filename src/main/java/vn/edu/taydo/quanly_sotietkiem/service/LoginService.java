package vn.edu.taydo.quanly_sotietkiem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.TaiKhoan;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.NhanVienRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.TaiKhoanRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    public String login(String username, String password, HttpServletResponse response) {
        // Kiểm tra khách hàng theo CCCD
        var khOpt = khachHangRepository.findByCccd(username);
        if (khOpt.isPresent()) {
            var kh = khOpt.get();
            var tkOpt = taiKhoanRepository.findById(kh.getTaikhoan_id());
            if (tkOpt.isPresent() && tkOpt.get().getPassword().equals(password)) {
                return setJwtCookie(response, tkOpt.get(), kh.getId());
            }
            return "Sai CCCD hoặc mật khẩu!";
        }

        // Kiểm tra nhân viên theo Mã nhân viên
        var nvOpt = nhanVienRepository.findByMaNhanVien(username);
        if (nvOpt.isPresent()) {
            var nv = nvOpt.get();
            var tkOpt = taiKhoanRepository.findById(nv.getTaikhoan_id());
            if (tkOpt.isPresent() && tkOpt.get().getPassword().equals(password)) {
                return setJwtCookie(response, tkOpt.get(), nv.getId());
            }
            return "Sai Mã nhân viên hoặc mật khẩu!";
        }

        return "Không tìm thấy người dùng!";
    }

    private String setJwtCookie(HttpServletResponse response, TaiKhoan tk, String realUserId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", tk.getRole());
        claims.put("userId", realUserId); // lưu ID KhachHang hoặc NhanVien

        String token = JwtUtil.generateToken(claims);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(4 * 60 * 60); // 4h
        response.addCookie(cookie);

        // Điều hướng theo role
        if ("ADMIN".equals(tk.getRole())) {
            return "redirect:/admin"; // chuyển hẳn sang dashboard admin
        } else if ("K_H".equals(tk.getRole())) {
            return "redirect:/"; // chuyển hẳn sang dashboard client
        }
        return "redirect:/login?error=role";
    }
}
