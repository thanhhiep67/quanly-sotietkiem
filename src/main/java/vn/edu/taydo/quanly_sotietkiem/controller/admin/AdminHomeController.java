package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.taydo.quanly_sotietkiem.DTO.YeuCauViewAdmin;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.*;
import vn.edu.taydo.quanly_sotietkiem.repository.*;
import vn.edu.taydo.quanly_sotietkiem.service.AdminHomeService;
import vn.edu.taydo.quanly_sotietkiem.service.HomeAdminService;
import vn.edu.taydo.quanly_sotietkiem.service.BaoCaoService;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    @Autowired
    private YeuCauMoSoRepository yeuCauMoSoRepository;
    @Autowired
    private SoTietKiemRepository soTietKiemRepository;
    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HomeAdminService homeAdminService;
    @Autowired
    private GiaoDichRepository giaoDichRepository;
    @Autowired
    private BaoCaoService baoCaoService;
    @Autowired
    private AdminHomeService adminHomeService;

    @GetMapping("")
    public String admin(Model model,
                        @RequestParam(defaultValue = "7") int days,
                        HttpServletRequest request) {

        // Yêu cầu mở sổ chờ duyệt
        List<YeuCauMoSo> list = yeuCauMoSoRepository.findByTrangThaiOrderByCreatedAtDesc("CHO");
        List<YeuCauViewAdmin> viewList = new ArrayList<>();
        for (YeuCauMoSo yc : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            KhachHang kh = khachHangRepository.findById(yc.getKhachHangId()).orElse(null);
            if (loai != null) {
                viewList.add(new YeuCauViewAdmin(yc, loai, kh));
            }
        }
        model.addAttribute("yeuCauList", viewList);

        // Thống kê tổng quan
        Double tongTaiSanHienTai = soTietKiemRepository.sumSoDuHienTaiDangHoatDong();
        long soDangHoatDong = soTietKiemRepository.countByTrangThai("MO");
        long tongKhachHang = khachHangRepository.count();
        model.addAttribute("tongTaiSanHienTai", tongTaiSanHienTai != null ? tongTaiSanHienTai : 0.0);
        model.addAttribute("soDangHoatDong", soDangHoatDong);
        model.addAttribute("tongKhachHang", tongKhachHang);

        // Thống kê loại sổ
        List<Map<String,Object>> thongKeLoaiSo = homeAdminService.thongKeLoaiSo();
        model.addAttribute("thongKeLoaiSo", thongKeLoaiSo);

        // Báo cáo doanh số (7 ngày hoặc 30 ngày)
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);

        // KHÔNG cần gọi taoBaoCaoNgay nữa vì đã có Scheduled Task
        List<BaoCaoNgay> baoCaoList = baoCaoService.layBaoCao(start, end);
        model.addAttribute("baoCaoList", baoCaoList);
        model.addAttribute("days", days);

        // Chuẩn bị dữ liệu cho Chart.js
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        for (BaoCaoNgay bc : baoCaoList) {
            chartLabels.add(bc.getNgay().toString());
            chartValues.add(bc.getChenhLech());
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // Thông tin đăng nhập
        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        if (claims == null) {
            return "redirect:/login";
        }

        String userId = (String) claims.get("userId");
        String role = (String) claims.get("role");
        String tenNhanVien = adminHomeService.getTenNhanVien(userId);

        model.addAttribute("username", tenNhanVien);
        model.addAttribute("role", role);

        return "qlstk/admin-dashboard/index";
    }


    @PostMapping("/duyet-yeu-cau")
    public String duyetYeuCau(@RequestParam("id") String id, HttpServletRequest request) {
        YeuCauMoSo yc = yeuCauMoSoRepository.findById(id).orElse(null);
        String idNhanVien = homeAdminService.getTenNhanVien(request);

        if (yc != null && "CHO".equals(yc.getTrangThai())) {
            yc.setTrangThai("DUYET");
            yc.setNgayXuLy(new Date());
            yc.setNhanVienId(idNhanVien);
            yeuCauMoSoRepository.save(yc);

            // Tạo sổ tiết kiệm mới
            SoTietKiem stk = new SoTietKiem();
            long countByKhachHang = soTietKiemRepository.countByKhachHangId(yc.getKhachHangId());
            String maSo = "STK-" + yc.getKhachHangId() + "-" + String.format("%02d", countByKhachHang + 1);
            stk.setMaSo(maSo);
            stk.setKhachHangId(yc.getKhachHangId());
            stk.setLoaiSoId(yc.getLoaiSoId());
            Date ngayMoSo = new Date();
            stk.setNgayMoSo(ngayMoSo);

            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            if (loai != null && loai.getKyHanThang() != null && loai.getKyHanThang() > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ngayMoSo);
                cal.add(Calendar.MONTH, loai.getKyHanThang());
                stk.setNgayDaoHan(cal.getTime());
            } else {
                stk.setNgayDaoHan(null);
            }

            stk.setTrangThai("MO");
            stk.setSoTienBanDau(yc.getSoTienGuiBanDau());
            stk.setSoDuHienTai(yc.getSoTienGuiBanDau());
            stk.setLaiSuatApDung(yc.getLaiSuatApDung());
            stk.setYeuCauMoSoId(yc.getId());

            soTietKiemRepository.save(stk);

            yc.setSoTkId(stk.getMaSo());
            yeuCauMoSoRepository.save(yc);
        }
        return "redirect:/admin";
    }

    @PostMapping("/tu-choi-yeu-cau")
    public String tuChoiYeuCau(@RequestParam("id") String id,
                               @RequestParam("lyDoTuChoi") String lyDo) {
        YeuCauMoSo yc = yeuCauMoSoRepository.findById(id).orElse(null);
        if (yc != null && "CHO".equals(yc.getTrangThai())) {
            yc.setTrangThai("TU CHOI");
            yc.setNgayXuLy(new Date());
            yc.setLyDoTuChoi(lyDo);
            yeuCauMoSoRepository.save(yc);
        }
        return "redirect:/admin";
    }

    // Endpoint để tạo báo cáo ngày (có thể gọi tay hoặc chạy cron job)
    @PostMapping("/tao-bao-cao-ngay")
    @ResponseBody
    public BaoCaoNgay taoBaoCaoNgay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        return baoCaoService.taoBaoCaoNgay(ngay);
    }
}
