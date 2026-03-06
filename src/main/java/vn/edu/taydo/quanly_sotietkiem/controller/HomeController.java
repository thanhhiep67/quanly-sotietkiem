package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;
import vn.edu.taydo.quanly_sotietkiem.service.HomeService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;
    @Autowired
    private LaiSuatRepository laiSuatRepository;
    @Autowired
    private SoTietKiemRepository soTietKiemRepository;
    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;
    @Autowired
    private GiaoDichRepository giaoDichRepository;

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
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

        String formattedTime = now.format(formatter);

        model.addAttribute("lastUpdate", formattedTime);

        // Lấy danh sách sổ của khách hàng
        List<SoTietKiem> soList = soTietKiemRepository.findByKhachHangId(khachHangId);

        double laiDuKien = 0;
        Date now1 = new Date();

        for (SoTietKiem so : soList) {
            if (!"MO".equals(so.getTrangThai())) continue; // chỉ tính sổ đang mở

            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai == null) continue;

            // Sổ không kỳ hạn
            if (loai.getKyHanThang() == null || loai.getKyHanThang() == 0) {
                long days = (now1.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
                double laiSuatKhongKyHan = 0.005; // 0.5%/năm — giống GiaoDichController
                laiDuKien += so.getSoDuHienTai() * (laiSuatKhongKyHan / 100.0) * (days / 365.0);
            }
            // Sổ có kỳ hạn
            else {
                LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
                if (laiSuat == null) continue;
                double laiSuatKyHan = laiSuat.getLaiSuatNam();
                double months = loai.getKyHanThang();
                laiDuKien += so.getSoTienBanDau() * (laiSuatKyHan / 100.0) * (months / 12.0);
            }
        }

        model.addAttribute("laiDuKien", (long) laiDuKien);

        List<GiaoDich> lichSu = giaoDichRepository.findTop5ByKhachHangIdOrderByCreatedAtDesc(khachHangId);
        model.addAttribute("lichSuGiaoDich", lichSu);

        return "qlstk/client/index";
    }
}
