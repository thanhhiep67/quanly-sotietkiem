package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.TaiKhoan;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.TaiKhoanRepository;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.util.Map;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private TaiKhoanRepository  taiKhoanRepository;
    @Autowired private ProfileService      profileService;

    // ─────────────────────────────────────────────────────────
    //  Helper: load KhachHang + sidebar attributes vào model
    //  Trả về KhachHang, hoặc null nếu chưa đăng nhập
    // ─────────────────────────────────────────────────────────
    private KhachHang loadCommonModel(HttpServletRequest request, Model model) {
        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return null;

        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        String role = claims != null ? (String) claims.get("role") : "";

        model.addAttribute("khachHang",    kh);
        model.addAttribute("tenKhachHang", kh.getHoten());
        model.addAttribute("role",         role);

        return kh;
    }

    // ─────────────────────────────────────────────────────────
    //  GET /profile  →  trang xem hồ sơ
    // ─────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String viewProfile(HttpServletRequest request, Model model) {
        KhachHang kh = loadCommonModel(request, model);
        if (kh == null) return "redirect:/login";
        return "qlstk/client/profile";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /profile/edit  →  trang chỉnh sửa
    // ─────────────────────────────────────────────────────────
    @GetMapping("/edit-profile")
    public String editForm(HttpServletRequest request, Model model) {
        KhachHang kh = loadCommonModel(request, model);
        if (kh == null) return "redirect:/login";
        return "qlstk/client/profile-edit";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /profile/edit  →  lưu thay đổi
    // ─────────────────────────────────────────────────────────
    @PostMapping("/edit-profile")
    public String saveEdit(
            HttpServletRequest request,
            RedirectAttributes redirectAttrs,

            @RequestParam(name = "hoten",          required = false) String hoten,
            @RequestParam(name = "gioiTinh",        required = false) String gioiTinh,
            @RequestParam(name = "sdt",             required = false) String sdt,
            @RequestParam(name = "email",           required = false) String email,
            @RequestParam(name = "diaChi",          required = false) String diaChi,

            @RequestParam(name = "matKhauCu",      required = false, defaultValue = "") String matKhauCu,
            @RequestParam(name = "matKhauMoi",     required = false, defaultValue = "") String matKhauMoi,
            @RequestParam(name = "xacNhanMatKhau", required = false, defaultValue = "") String xacNhanMatKhau
    ) {
        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return "redirect:/login";

        // ── Validate ─────────────────────────────────────────
        if (hoten == null || hoten.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Họ tên không được để trống.");
            return "redirect:/profile/edit";
        }
        if (sdt == null || sdt.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Số điện thoại không được để trống.");
            return "redirect:/profile/edit";
        }
        if (email == null || email.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Email không được để trống.");
            return "redirect:/profile/edit";
        }

        // ── Cập nhật thông tin cá nhân ───────────────────────
        kh.setHoten(hoten.trim());
        kh.setGioiTinh(gioiTinh);
        kh.setSdt(sdt.trim());
        kh.setEmail(email.trim());
        kh.setDiaChi(diaChi != null ? diaChi.trim() : null);
        khachHangRepository.save(kh);

        // ── Đổi mật khẩu (nếu nhập) ──────────────────────────
        if (!matKhauMoi.isBlank()) {
            if (matKhauMoi.length() < 6) {
                redirectAttrs.addFlashAttribute("errorMsg", "Mật khẩu mới phải có ít nhất 6 ký tự.");
                return "redirect:/profile/edit";
            }
            if (!matKhauMoi.equals(xacNhanMatKhau)) {
                redirectAttrs.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp.");
                return "redirect:/profile/edit";
            }

            Optional<TaiKhoan> tkOpt = taiKhoanRepository.findById(kh.getTaikhoan_id());
            if (tkOpt.isEmpty()) {
                redirectAttrs.addFlashAttribute("errorMsg", "Không tìm thấy tài khoản đăng nhập.");
                return "redirect:/profile/edit";
            }

            TaiKhoan tk = tkOpt.get();
            if (!matKhauCu.equals(tk.getPassword())) {
                redirectAttrs.addFlashAttribute("errorMsg", "Mật khẩu hiện tại không đúng.");
                return "redirect:/profile/edit";
            }

            tk.setPassword(matKhauMoi);
            taiKhoanRepository.save(tk);
        }

        redirectAttrs.addFlashAttribute("successMsg", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile";
    }
}