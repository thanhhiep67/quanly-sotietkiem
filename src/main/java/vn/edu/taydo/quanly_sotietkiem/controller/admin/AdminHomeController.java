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
        // Chỉ lấy các yêu cầu có trạng thái CHO
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

            // Sinh mã sổ theo dạng STK-KHxxx-xx
            long countByKhachHang = soTietKiemRepository.countByKhachHangId(yc.getKhachHangId());
            String maSo = "STK-" + yc.getKhachHangId() + "-" + String.format("%02d", countByKhachHang + 1);
            stk.setMaSo(maSo);

            stk.setKhachHangId(yc.getKhachHangId());
            stk.setLoaiSoId(yc.getLoaiSoId());
            Date ngayMoSo = new Date();
            stk.setNgayMoSo(ngayMoSo);

            // Lấy loại sổ để biết kỳ hạn
            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            if (loai != null && loai.getKyHanThang() != null && loai.getKyHanThang() > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ngayMoSo);
                cal.add(Calendar.MONTH, loai.getKyHanThang()); // cộng số tháng kỳ hạn
                stk.setNgayDaoHan(cal.getTime());
            } else {
                stk.setNgayDaoHan(null); // không kỳ hạn
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
}
