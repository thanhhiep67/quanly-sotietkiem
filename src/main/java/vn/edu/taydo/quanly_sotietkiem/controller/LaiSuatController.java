package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuatView;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class LaiSuatController {

    @Autowired private LoaiSoTKRepository loaiSoTKRepository;
    @Autowired private LaiSuatRepository  laiSuatRepository;
    @Autowired private ProfileService     profileService;

    @GetMapping("/lai-suat")
    public String laiSuat(Model model, HttpServletRequest request) {

        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return "redirect:/login";

        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        String role = claims != null ? (String) claims.get("role") : "";

        model.addAttribute("khachHang",    kh);
        model.addAttribute("tenKhachHang", kh.getHoten());
        model.addAttribute("role",         role);

        // Lãi suất mới nhất cho từng loại sổ
        List<LoaiSoTK> loaiSoList = loaiSoTKRepository.findAll();
        List<LaiSuatView> viewList = new ArrayList<>();
        for (LoaiSoTK loai : loaiSoList) {
            LaiSuat laiSuat = laiSuatRepository
                    .findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            viewList.add(new LaiSuatView(loai, laiSuat));
        }

        model.addAttribute("laiSuatList",  viewList);
        model.addAttribute("ngayCapNhat",  LocalDate.now());

        return "qlstk/client/lai-suat";
    }
}