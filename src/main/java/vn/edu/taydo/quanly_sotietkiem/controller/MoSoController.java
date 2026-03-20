package vn.edu.taydo.quanly_sotietkiem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.taydo.quanly_sotietkiem.DTO.LoaiSoView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.repository.YeuCauMoSoRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;

import jakarta.servlet.http.HttpServletRequest;
import vn.edu.taydo.quanly_sotietkiem.service.ProfileService;

import java.util.*;

@Controller
public class MoSoController {

    @Autowired private LoaiSoTKRepository    loaiSoTKRepository;
    @Autowired private LaiSuatRepository     laiSuatRepository;
    @Autowired private YeuCauMoSoRepository  yeuCauMoSoRepository;
    @Autowired private ProfileService        profileService;

    // ─────────────────────────────────────────────────────────
    //  GET /mo-so-tiet-kiem
    // ─────────────────────────────────────────────────────────
    @GetMapping("/mo-so-tiet-kiem")
    public String moSoTietKiem(Model model, HttpServletRequest request) {

        KhachHang kh = profileService.getKhachHangFromRequest(request);
        if (kh == null) return "redirect:/login";

        Map<String, Object> claims = JwtUtil.getClaimsFromCookie(request);
        String role = claims != null ? (String) claims.get("role") : "";

        model.addAttribute("khachHang",    kh);
        model.addAttribute("tenKhachHang", kh.getHoten());
        model.addAttribute("role",         role);

        // Lãi suất mới nhất cho từng loại sổ
        List<LoaiSoTK> loaiSoList = loaiSoTKRepository.findAll();
        List<LoaiSoView> viewList = new ArrayList<>();
        for (LoaiSoTK loai : loaiSoList) {
            LaiSuat laiSuat = laiSuatRepository
                    .findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            viewList.add(new LoaiSoView(loai, laiSuat));
        }

        model.addAttribute("loaiSoList", viewList);
        model.addAttribute("ngayMoSo",   new Date());

        return "qlstk/client/mo-so-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /mo-so-tiet-kiem
    // ─────────────────────────────────────────────────────────
    @PostMapping("/mo-so-tiet-kiem")
    public String xuLyMoSo(@RequestParam String loaiSoId,
                           @RequestParam double soTienGui,
                           HttpServletRequest request) {

        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) return "redirect:/login";

        LaiSuat laiSuat = laiSuatRepository
                .findTopByLoaiStkIdOrderByNgayApDungDesc(loaiSoId);

        YeuCauMoSo yc = new YeuCauMoSo();
        yc.setKhachHangId(khachHangId);
        yc.setLoaiSoId(loaiSoId);
        yc.setSoTienGuiBanDau(soTienGui);
        yc.setTrangThai("CHO");
        yc.setCreatedAt(new Date());
        yc.setLaiSuatApDung(laiSuat != null ? laiSuat.getLaiSuatNam() : null);

        yeuCauMoSoRepository.save(yc);

        return "redirect:/tra-cuu-yeu-cau";
    }
}