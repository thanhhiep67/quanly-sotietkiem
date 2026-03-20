package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.*;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;
import vn.edu.taydo.quanly_sotietkiem.service.HomeService;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @Autowired
    private ProfileService profileService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {


        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return "redirect:/login";

        model.addAttribute("khachHang", kh);

        // ── Xác thực JWT ──────────────────────────────────────────
        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        if (claims == null) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            return "redirect:/login";
        }

        String role        = (String) claims.get("role");
        String khachHangId = (String) claims.get("userId");

        if (!"K_H".equals(role)) {
            return "redirect:/login?error=unauthorized";
        }

        // ── Thông tin khách hàng ──────────────────────────────────
        String tenKhachHang = homeService.getTenKhachHang(request);
        if (tenKhachHang == null) {
            model.addAttribute("errorMessage", "Không tìm thấy khách hàng hoặc chưa đăng nhập!");
            return "qlstk/client/index";
        }

        model.addAttribute("tenKhachHang", tenKhachHang);
        model.addAttribute("role", role);

        // ── Tổng tài sản & số sổ ─────────────────────────────────
        double tongTaiSan = homeService.getTongTaiSanTietKiem(khachHangId);
        int    soLuongSo  = homeService.getSoLuongSoTietKiem(khachHangId);
        model.addAttribute("tongTaiSan", tongTaiSan);
        model.addAttribute("soLuongSo",  soLuongSo);

        // ── Thời gian cập nhật ────────────────────────────────────
        model.addAttribute("lastUpdate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")));

        // ── Lãi dự kiến ──────────────────────────────────────────
        List<SoTietKiem> soList = soTietKiemRepository.findByKhachHangId(khachHangId);
        Date now = new Date();
        double laiDuKien = 0;

        for (SoTietKiem so : soList) {
            if (!"MO".equals(so.getTrangThai())) continue;

            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai == null) continue;

            if (loai.getKyHanThang() == null || loai.getKyHanThang() == 0) {
                long days = (now.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
                laiDuKien += so.getSoDuHienTai() * (0.005 / 100.0) * (days / 365.0);
            } else {
                LaiSuat laiSuat = laiSuatRepository
                        .findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
                if (laiSuat == null) continue;
                laiDuKien += so.getSoDuHienTai()
                        * (laiSuat.getLaiSuatNam() / 100.0)
                        * (loai.getKyHanThang() / 12.0);
            }
        }
        model.addAttribute("laiDuKien", (long) laiDuKien);

        // ── Giao dịch gần đây ────────────────────────────────────
        model.addAttribute("lichSuGiaoDich",
                giaoDichRepository.findTop5ByKhachHangIdOrderByCreatedAtDesc(khachHangId));

        // ── Dữ liệu biểu đồ: tổng tài sản theo 6 tháng gần nhất ─
        //
        //  Cách tính:
        //  Với mỗi tháng T trong 6 tháng qua, lấy tất cả sổ đang
        //  tồn tại tại thời điểm cuối tháng T (ngayMoSo <= cuối T)
        //  và tính tổng soDuHienTai của chúng.
        //
        //  Nếu sổ đã tất toán (DONG) và ngayTatToan < cuối T thì
        //  không tính — hiện tại model SoTietKiem chưa lưu
        //  ngayTatToan nên ta dùng soDuHienTai cho sổ đang mở,
        //  và soTienBanDau cho sổ đã đóng (ước tính bảo thủ).
        // ─────────────────────────────────────────────────────────

        List<String> chartLabels = new ArrayList<>();
        List<Long>   chartValues = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM/yy");

        for (int i = 5; i >= 0; i--) {
            YearMonth  ym      = currentMonth.minusMonths(i);
            LocalDate  lastDay = ym.atEndOfMonth();
            Date       lastDayDate = java.sql.Date.valueOf(lastDay);

            long totalForMonth = 0L;
            for (SoTietKiem so : soList) {
                // Sổ phải được mở trước hoặc trong tháng này
                if (so.getNgayMoSo().after(lastDayDate)) continue;

                if ("MO".equals(so.getTrangThai())) {
                    totalForMonth += (long) so.getSoDuHienTai();
                } else {
                    // Sổ đã đóng: chỉ tính nếu mở trước tháng này
                    // (dùng soTienBanDau vì không có ngayTatToan)
                    totalForMonth += (long) so.getSoTienBanDau();
                }
            }

            chartLabels.add("T" + ym.format(labelFmt));   // VD: "T03/26"
            chartValues.add(totalForMonth);
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        return "qlstk/client/index";
    }
}
