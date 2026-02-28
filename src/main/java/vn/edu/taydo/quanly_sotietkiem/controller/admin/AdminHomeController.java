package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.taydo.quanly_sotietkiem.DTO.YeuCauView;
import vn.edu.taydo.quanly_sotietkiem.DTO.YeuCauViewAdmin;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.YeuCauMoSoRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;
import vn.edu.taydo.quanly_sotietkiem.service.HomeAdminService;
import vn.edu.taydo.quanly_sotietkiem.service.HomeService;

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
    KhachHangRepository khachHangRepository;
    @Autowired
    HomeAdminService homeAdminService;
    @GetMapping("")
    public String admin(Model model) {
        List<YeuCauMoSo> list = yeuCauMoSoRepository.findAll();
        List<YeuCauViewAdmin> viewList = new ArrayList<>();

        for (YeuCauMoSo yc : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            KhachHang kh = khachHangRepository.findById(yc.getKhachHangId()).orElse(null);
            if (loai != null) {
                viewList.add(new YeuCauViewAdmin(yc, loai, kh));
            }
        }

        model.addAttribute("yeuCauList", viewList);
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
            stk.setMaSo(UUID.randomUUID().toString());
            stk.setKhachHangId(yc.getKhachHangId());
            stk.setLoaiSoId(yc.getLoaiSoId());
            stk.setNgayMoSo(new Date());

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 6); // ví dụ: thời hạn 6 tháng
            stk.setNgayDaoHan(cal.getTime());
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
}
