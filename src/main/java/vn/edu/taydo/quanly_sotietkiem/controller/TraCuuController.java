package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.DTO.SoTietKiemView;
import vn.edu.taydo.quanly_sotietkiem.DTO.YeuCauView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.YeuCauMoSoRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TraCuuController {

    @Autowired
    private YeuCauMoSoRepository yeuCauMoSoRepository;

    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;
    @Autowired
    private SoTietKiemRepository soTietKiemRepository;
    @GetMapping("/tra-cuu-so")
    public String traCuu(HttpServletRequest request, Model model) {

        String khachHangId = JwtUtil.getUserIdFromCookie(request);

        if (khachHangId == null) {
            return "redirect:/login";
        }

        List<SoTietKiem> list = soTietKiemRepository.findByKhachHangId(khachHangId);

        List<SoTietKiemView> viewList = new ArrayList<>();

        for (SoTietKiem so : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai != null) {
                viewList.add(new SoTietKiemView(so, loai));
            }
        }

        model.addAttribute("danhSachSo", viewList);

        model.addAttribute("danhSachSo", viewList);

        return "qlstk/client/tra-cuu-so";
    }

    @GetMapping("/tra-cuu-yeu-cau")
    public String traCuuYeuCau(HttpServletRequest request, Model model) {
        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        List<YeuCauMoSo> list = yeuCauMoSoRepository.findByKhachHangId(khachHangId);

        List<YeuCauView> viewList = new ArrayList<>();
        for (YeuCauMoSo yc : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(yc.getLoaiSoId()).orElse(null);
            if (loai != null) {
                viewList.add(new YeuCauView(yc, loai));
            }
        }

        model.addAttribute("yeuCauList", viewList);
        return "qlstk/client/tra-cuu-yeu-cau";
    }

}
