package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.taydo.quanly_sotietkiem.DTO.SoTietKiemView;
import vn.edu.taydo.quanly_sotietkiem.DTO.YeuCauView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.*;
import vn.edu.taydo.quanly_sotietkiem.repository.*;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class TraCuuController {

    @Autowired private YeuCauMoSoRepository  yeuCauMoSoRepository;
    @Autowired private LoaiSoTKRepository    loaiSoTKRepository;
    @Autowired private SoTietKiemRepository  soTietKiemRepository;
    @Autowired private GiaoDichRepository    giaoDichRepository;
    @Autowired private LaiSuatRepository     laiSuatRepository;
    @Autowired private ProfileService        profileService;

    // ─────────────────────────────────────────────────────────
    //  Helper: load sidebar attributes, trả về khachHangId
    // ─────────────────────────────────────────────────────────
    private String loadCommonModel(HttpServletRequest request, Model model) {
        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return null;

        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        String role = claims != null ? (String) claims.get("role") : "";

        model.addAttribute("khachHang",    kh);
        model.addAttribute("tenKhachHang", kh.getHoten());
        model.addAttribute("role",         role);

        return kh.getId();
    }

    // ─────────────────────────────────────────────────────────
    //  GET /tra-cuu-so
    // ─────────────────────────────────────────────────────────
    @GetMapping("/tra-cuu-so")
    public String traCuu(HttpServletRequest request, Model model) {
        String khachHangId = loadCommonModel(request, model);
        if (khachHangId == null) return "redirect:/login";

        List<SoTietKiem> list = soTietKiemRepository.findByKhachHangId(khachHangId);
        List<SoTietKiemView> viewList = new ArrayList<>();
        for (SoTietKiem so : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai != null) viewList.add(new SoTietKiemView(so, loai));
        }

        model.addAttribute("danhSachSo", viewList);
        return "qlstk/client/tra-cuu-so";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /tra-cuu-yeu-cau
    // ─────────────────────────────────────────────────────────
    @GetMapping("/tra-cuu-yeu-cau")
    public String traCuuYeuCau(HttpServletRequest request, Model model) {
        String khachHangId = loadCommonModel(request, model);
        if (khachHangId == null) return "redirect:/login";

        List<YeuCauMoSo> list = yeuCauMoSoRepository.findByKhachHangId(khachHangId);
        List<YeuCauView> viewList = new ArrayList<>();
        for (YeuCauMoSo yc : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            if (loai != null) viewList.add(new YeuCauView(yc, loai));
        }

        model.addAttribute("yeuCauList", viewList);
        return "qlstk/client/tra-cuu-yeu-cau";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /so-chi-tiet/{maSo}
    // ─────────────────────────────────────────────────────────
    @GetMapping("/so-chi-tiet/{maSo}")
    public String soChiTiet(@PathVariable String maSo,
                            HttpServletRequest request, Model model) {

        String khachHangId = loadCommonModel(request, model);
        if (khachHangId == null) return "redirect:/login";

        // Tìm sổ & kiểm tra quyền sở hữu
        SoTietKiem so = soTietKiemRepository.findByMaSo(maSo);
        if (so == null || !so.getKhachHangId().equals(khachHangId))
            return "redirect:/tra-cuu-so";

        LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
        if (loai == null) return "redirect:/tra-cuu-so";

        model.addAttribute("so",   so);
        model.addAttribute("loai", loai);

        // ── Tính lãi ─────────────────────────────────────────
        Date now = new Date();
        double laiTichLuy   = 0;
        double laiDenDaoHan = 0;

        boolean coKyHan = loai.getKyHanThang() != null && loai.getKyHanThang() > 0;

        if (coKyHan) {
            LaiSuat laiSuat = laiSuatRepository
                    .findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            double laiSuatNam = laiSuat != null
                    ? laiSuat.getLaiSuatNam()
                    : so.getLaiSuatApDung();

            long days = (now.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
            laiTichLuy  = so.getSoDuHienTai() * (laiSuatNam / 100.0) * (days / 365.0);
            laiDenDaoHan = so.getSoDuHienTai()
                    * (laiSuatNam / 100.0)
                    * (loai.getKyHanThang() / 12.0);
        } else {
            long days = (now.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
            laiTichLuy  = so.getSoDuHienTai() * (0.005 / 100.0) * (days / 365.0);
            laiDenDaoHan = laiTichLuy;
        }

        model.addAttribute("laiTichLuy",     (long) laiTichLuy);
        model.addAttribute("laiDenDaoHan",   (long) laiDenDaoHan);
        model.addAttribute("tongNhanDaoHan", (long)(so.getSoDuHienTai() + laiDenDaoHan));

        // ── Tiến độ thời gian (%) ─────────────────────────────
        int phanTram = 0;
        if (so.getNgayDaoHan() != null) {
            long total   = so.getNgayDaoHan().getTime() - so.getNgayMoSo().getTime();
            long elapsed = now.getTime()                - so.getNgayMoSo().getTime();
            phanTram = (int) Math.min(100, Math.max(0, elapsed * 100 / total));
        }
        model.addAttribute("phanTramThoiGian", phanTram);

        // ── Lịch sử giao dịch của sổ này ─────────────────────
        model.addAttribute("lichSu",
                giaoDichRepository.findAllBySoTkIdOrderByCreatedAtDesc(so.getMaSo()));

        // ── Biểu đồ: số dư sau mỗi giao dịch (10 gần nhất) ─
        List<GiaoDich> allGd = giaoDichRepository
                .findAllBySoTkIdOrderByCreatedAtDesc(so.getMaSo());

        // Lấy tối đa 10 giao dịch gần nhất, đảo lại để vẽ từ cũ → mới
        List<GiaoDich> top10 = allGd.stream()
                .limit(10)
                .sorted(Comparator.comparing(GiaoDich::getNgayGiaoDich))
                .collect(java.util.stream.Collectors.toList());

        List<String> chartLabels = new ArrayList<>();
        List<Long>   chartValues = new ArrayList<>();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("dd/MM");

        // Điểm đầu tiên: số dư ban đầu khi mở sổ
        chartLabels.add("Mở sổ");
        chartValues.add((long) so.getSoTienBanDau());

        // Replay từng giao dịch
        double soDu = so.getSoTienBanDau();
        for (GiaoDich gd : top10) {
            if ("NAP".equals(gd.getLoaiGiaoDich()))      soDu += gd.getSoTien();
            else if ("RUT".equals(gd.getLoaiGiaoDich())) soDu -= gd.getSoTien();

            String label = gd.getNgayGiaoDich().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().format(labelFmt);
            chartLabels.add(label);
            chartValues.add(Math.max(0L, (long) soDu));
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        return "qlstk/client/so-chi-tiet";
    }
}