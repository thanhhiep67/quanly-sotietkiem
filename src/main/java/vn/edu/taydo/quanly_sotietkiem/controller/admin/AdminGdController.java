package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/giao-dich")
public class AdminGdController {

    @Autowired private KhachHangRepository  khachHangRepository;
    @Autowired private SoTietKiemRepository soTietKiemRepository;
    @Autowired private GiaoDichRepository   giaoDichRepository;

    // ─────────────────────────────────────────────────────────
    //  GET /admin/giao-dich  →  trang mặc định
    // ─────────────────────────────────────────────────────────
    @GetMapping
    public String getGdPage() {
        return "qlstk/admin-dashboard/giao-dich";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /admin/giao-dich/tim-kiem?cccd=...
    // ─────────────────────────────────────────────────────────
    @GetMapping("/tim-kiem")
    public String timKiemTheoCccd(@RequestParam String cccd,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Optional<KhachHang> khOpt = khachHangRepository.findByCccd(cccd);
        if (khOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng với CCCD này!");
            return "redirect:/admin/giao-dich";
        }

        KhachHang kh = khOpt.get();
        List<SoTietKiem> soList = soTietKiemRepository.findByKhachHangId(kh.getId());
        List<GiaoDich>   gdList = giaoDichRepository
                .findByKhachHangIdOrderByNgayGiaoDichDesc(kh.getId());

        model.addAttribute("khachHang", kh);
        model.addAttribute("soList",    soList);
        model.addAttribute("gdList",    gdList);
        return "qlstk/admin-dashboard/giao-dich";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /admin/giao-dich/thuc-hien
    // ─────────────────────────────────────────────────────────
    @PostMapping("/thuc-hien")
    public String thucHienGiaoDich(
            @RequestParam String maSo,
            @RequestParam double soTien,
            @RequestParam String loai,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date ngayThucHien,
            RedirectAttributes redirectAttributes) {

        Optional<SoTietKiem> stkOpt = soTietKiemRepository.findById(maSo);
        if (stkOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sổ tiết kiệm!");
            return "redirect:/admin/giao-dich";
        }

        SoTietKiem stk = stkOpt.get();
        String redirectCccd = "redirect:/admin/giao-dich/tim-kiem?cccd="
                + getCccdByKhachHangId(stk.getKhachHangId());

        // ── Kiểm tra trạng thái sổ ───────────────────────────
        if (!"MO".equals(stk.getTrangThai())) {
            redirectAttributes.addFlashAttribute("error", "Sổ đã đóng, không thể thực hiện giao dịch!");
            return redirectCccd;
        }

        Date today = new Date();

        if ("RUT".equals(loai)) {
            if (stk.getNgayDaoHan() != null && today.before(stk.getNgayDaoHan())) {
                redirectAttributes.addFlashAttribute("error", "Chưa tới ngày đáo hạn, không thể rút!");
                return redirectCccd;
            }
            if (stk.getSoDuHienTai() < soTien) {
                redirectAttributes.addFlashAttribute("error", "Số dư không đủ để rút!");
                return redirectCccd;
            }
            stk.setSoDuHienTai(stk.getSoDuHienTai() - soTien);
            if (stk.getSoDuHienTai() == 0) {
                stk.setTrangThai("DONG");
                stk.setNgayDongSo(LocalDate.now());
            }
        } else if ("GUI".equals(loai) || "NAP".equals(loai)) {
            stk.setSoDuHienTai(stk.getSoDuHienTai() + soTien);
        }

        // ── Lưu giao dịch ────────────────────────────────────
        GiaoDich gd = new GiaoDich();
        gd.setSoTkId(maSo);
        gd.setKhachHangId(stk.getKhachHangId());
        gd.setSoTien(soTien);
        gd.setLoaiGiaoDich(loai);
        gd.setGhiChu("RUT".equals(loai) ? "Rút tiền tiết kiệm" : "Gửi tiền tiết kiệm");
        gd.setTrangThai("COMPLETED");
        gd.setCreatedAt(new Date());
        gd.setNgayGiaoDich(ngayThucHien != null ? ngayThucHien : today);
        giaoDichRepository.save(gd);

        soTietKiemRepository.save(stk);

        redirectAttributes.addFlashAttribute("message", "Thực hiện giao dịch thành công!");
        return redirectCccd;
    }

    // ─────────────────────────────────────────────────────────
    //  Helper: lấy CCCD từ khachHangId
    // ─────────────────────────────────────────────────────────
    private String getCccdByKhachHangId(String khachHangId) {
        return khachHangRepository.findById(khachHangId)
                .map(KhachHang::getCccd)
                .orElse("");
    }
}