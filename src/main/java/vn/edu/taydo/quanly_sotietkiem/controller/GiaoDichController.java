package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.DTO.SoTietKiemView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.*;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.time.LocalDate;
import java.util.*;

@Controller
public class GiaoDichController {

    @Autowired private SoTietKiemRepository soTietKiemRepository;
    @Autowired private LoaiSoTKRepository   loaiSoTKRepository;
    @Autowired private GiaoDichRepository   giaoDichRepository;
    @Autowired private LaiSuatRepository    laiSuatRepository;
    @Autowired private ProfileService       profileService;

    // ─────────────────────────────────────────────────────────
    //  Helper: load thông tin chung vào model (sidebar, v.v.)
    // ─────────────────────────────────────────────────────────
    private String loadCommonModel(HttpServletRequest request, Model model) {
        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return null; // signal redirect

        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        String role = claims != null ? (String) claims.get("role") : "";

        model.addAttribute("khachHang",     kh);
        model.addAttribute("tenKhachHang",  kh.getHoten());
        model.addAttribute("role",          role);

        return kh.getId(); // trả về khachHangId
    }

    // ─────────────────────────────────────────────────────────
    //  GET /giao-dich
    // ─────────────────────────────────────────────────────────
    @GetMapping("/giao-dich")
    public String giaoDich(HttpServletRequest request, Model model) {

        String khachHangId = loadCommonModel(request, model);
        if (khachHangId == null) return "redirect:/login";

        // Danh sách sổ tiết kiệm
        List<SoTietKiem> list = soTietKiemRepository.findByKhachHangId(khachHangId);
        List<SoTietKiemView> viewList = new ArrayList<>();
        for (SoTietKiem so : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai != null) viewList.add(new SoTietKiemView(so, loai));
        }
        model.addAttribute("soList", viewList);

        // Lịch sử giao dịch
        List<GiaoDich> lichSu = giaoDichRepository
                .findByKhachHangIdOrderByCreatedAtDesc(khachHangId);
        model.addAttribute("lichSuGiaoDich", lichSu);

        return "qlstk/client/giao-dich";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /rut-tien
    // ─────────────────────────────────────────────────────────
    @PostMapping("/rut-tien")
    public String rutTien(HttpServletRequest request,
                          @RequestParam String soTkId,
                          @RequestParam double soTien,
                          RedirectAttributes ra) {

        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) return "redirect:/login";

        SoTietKiem so = soTietKiemRepository.findByMaSo(soTkId);
        if (so == null || !so.getKhachHangId().equals(khachHangId)) {
            ra.addFlashAttribute("error", "Không tìm thấy sổ tiết kiệm hợp lệ");
            return "redirect:/giao-dich";
        }
        if (!"MO".equals(so.getTrangThai())) {
            ra.addFlashAttribute("error", "Sổ đã đóng, không thể thực hiện giao dịch");
            return "redirect:/giao-dich";
        }

        LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
        if (loai == null) {
            ra.addFlashAttribute("error", "Không xác định được loại sổ");
            return "redirect:/giao-dich";
        }

        Date now = new Date();
        double tienNhan = 0;
        double tienLai  = 0;

        // ── Không kỳ hạn ────────────────────────────────────
        if (loai.getKyHanThang() == null || loai.getKyHanThang() == 0) {
            long days = (now.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
            if (days < 15) {
                ra.addFlashAttribute("error", "Sổ không kỳ hạn chỉ được rút sau 15 ngày");
                return "redirect:/giao-dich";
            }
            if (soTien > so.getSoDuHienTai()) {
                ra.addFlashAttribute("error", "Số tiền rút vượt quá số dư");
                return "redirect:/giao-dich";
            }

            double laiSuatKhongKyHan = 0.005; // 0.5%/năm
            tienLai  = soTien * (laiSuatKhongKyHan / 100.0) * (days / 365.0);
            tienNhan = soTien + tienLai;

            so.setSoDuHienTai(so.getSoDuHienTai() - soTien);
            if (so.getSoDuHienTai() == 0) so.setTrangThai("DONG");
        }
        // ── Có kỳ hạn ───────────────────────────────────────
        else {
            Date ngayDaoHan = so.getNgayDaoHan();
            if (ngayDaoHan == null) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn chưa có ngày đáo hạn");
                return "redirect:/giao-dich";
            }
            if (now.before(ngayDaoHan)) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn chỉ được rút khi đến hoặc quá ngày đáo hạn");
                return "redirect:/giao-dich";
            }
            if (soTien < so.getSoDuHienTai()) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn phải rút toàn bộ số dư");
                return "redirect:/giao-dich";
            }

            // Đúng hạn → lãi suất kỳ hạn
            if (Math.abs(now.getTime() - ngayDaoHan.getTime()) < 24L * 60 * 60 * 1000) {
                LaiSuat laiSuat = laiSuatRepository
                        .findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
                if (laiSuat == null) {
                    ra.addFlashAttribute("error", "Không tìm thấy lãi suất áp dụng");
                    return "redirect:/giao-dich";
                }
                tienLai = so.getSoDuHienTai()
                        * (laiSuat.getLaiSuatNam() / 100.0)
                        * (loai.getKyHanThang() / 12.0);
            } else {
                // Quá hạn → lãi suất không kỳ hạn
                tienLai = so.getSoDuHienTai() * 0.005;
            }

            tienNhan = so.getSoDuHienTai() + tienLai;
            so.setSoDuHienTai(0);
            so.setTrangThai("DONG");
            so.setNgayDongSo(LocalDate.now());
        }

        soTietKiemRepository.save(so);

        // Tạo giao dịch
        GiaoDich gd = new GiaoDich();
        gd.setSoTkId(soTkId);
        gd.setKhachHangId(khachHangId);
        gd.setLoaiGiaoDich("RUT");
        gd.setNgayGiaoDich(now);
        gd.setSoTien(soTien);
        gd.setTienLaiPhatSinh(tienLai);
        gd.setTongTienNhan(tienNhan);
        gd.setCreatedAt(now);
        gd.setTrangThai("COMPLETED");
        gd.setGhiChu("Rút tiền tiết kiệm");
        giaoDichRepository.save(gd);

        ra.addFlashAttribute("success",
                String.format("Rút tiền thành công! Nhận: %,.0f đ (gốc) + %,.0f đ (lãi) = %,.0f đ",
                        soTien, tienLai, tienNhan));
        return "redirect:/giao-dich";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /nap-tien
    // ─────────────────────────────────────────────────────────
    @PostMapping("/nap-tien")
    public String napTien(HttpServletRequest request,
                          @RequestParam String soTkId,
                          @RequestParam double soTien,
                          RedirectAttributes ra) {

        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) return "redirect:/login";

        SoTietKiem so = soTietKiemRepository.findByMaSo(soTkId);
        if (so == null || !so.getKhachHangId().equals(khachHangId)) {
            ra.addFlashAttribute("errorNap", "Không tìm thấy sổ tiết kiệm hợp lệ");
            return "redirect:/giao-dich";
        }
        if (!"MO".equals(so.getTrangThai())) {
            ra.addFlashAttribute("errorNap", "Sổ không còn hoạt động để nạp thêm tiền");
            return "redirect:/giao-dich";
        }

        so.setSoDuHienTai(so.getSoDuHienTai() + soTien);
        soTietKiemRepository.save(so);

        GiaoDich gd = new GiaoDich();
        gd.setKhachHangId(khachHangId);
        gd.setSoTkId(soTkId);
        gd.setSoTien(soTien);
        gd.setLoaiGiaoDich("NAP");
        gd.setTrangThai("COMPLETED");
        gd.setCreatedAt(new Date());
        gd.setNgayGiaoDich(new Date());
        gd.setGhiChu("Gửi tiền tiết kiệm");
        giaoDichRepository.save(gd);

        ra.addFlashAttribute("successNap",
                String.format("Nạp tiền thành công! +%,.0f đ", soTien));
        return "redirect:/giao-dich";
    }
}