package vn.edu.taydo.quanly_sotietkiem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.TaiKhoan;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.TaiKhoanRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class RegisterController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @GetMapping("/register")
    public String register() {
        return "qlstk/client/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String hoten,
                           @RequestParam String cccd,
                           @RequestParam String sdt,
                           @RequestParam(required = false) String diaChi,
                           @RequestParam(required = false) String ngaySinh,
                           @RequestParam String gioiTinh,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {

        if (khachHangRepository.findByCccd(cccd).isPresent()) {
            model.addAttribute("errorMessage", "CCCD này đã tồn tại trong hệ thống!");
            return "register"; // quay lại trang đăng ký
        }
        // Kiểm tra mật khẩu xác nhận
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "register"; // quay lại trang register.html
        }

        // Tạo tài khoản
        TaiKhoan tk = new TaiKhoan();
        tk.setId("TK" + UUID.randomUUID().toString().substring(0, 5));
        tk.setRole("K_H");
        tk.setPassword(password);
        tk.setCreatedAt(LocalDateTime.now());
        taiKhoanRepository.save(tk);

        // Tạo khách hàng
        KhachHang kh = new KhachHang();
        kh.setId("KH" + UUID.randomUUID().toString().substring(0, 5));
        kh.setTaikhoan_id(tk.getId());
        kh.setHoten(hoten);
        kh.setCccd(cccd);
        kh.setCccd(email);
        kh.setSdt(sdt);
        kh.setGioiTinh(gioiTinh);
        kh.setDiaChi(diaChi);
        if (ngaySinh != null && !ngaySinh.isEmpty()) { kh.setNgaySinh(LocalDate.parse(ngaySinh)); } // nếu bạn để LocalDate thì cần parse
        kh.setCreatedAt(LocalDateTime.now());
        khachHangRepository.save(kh);

        // Thông báo thành công
        model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "qlstk/client/login"; // chuyển sang trang login.html
    }
}
