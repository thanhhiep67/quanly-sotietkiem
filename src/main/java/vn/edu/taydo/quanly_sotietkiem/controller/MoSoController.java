package vn.edu.taydo.quanly_sotietkiem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.taydo.quanly_sotietkiem.DTO.LoaiSoView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.repository.YeuCauMoSoRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class MoSoController {

    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;

    @Autowired
    private LaiSuatRepository laiSuatRepository;

    @Autowired
    private YeuCauMoSoRepository yeuCauMoSoRepository;

    // Hiển thị form mở sổ
    @GetMapping("/mo-so-tiet-kiem")
    public String moSoTietKiem(Model model) {
        List<LoaiSoTK> loaiSoList = loaiSoTKRepository.findAll();

        // Lấy lãi suất mới nhất cho từng loại sổ
        List<LoaiSoView> viewList = new ArrayList<>();
        for (LoaiSoTK loai : loaiSoList) {
            LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            viewList.add(new LoaiSoView(loai, laiSuat));
        }

        model.addAttribute("loaiSoList", viewList);
        model.addAttribute("ngayMoSo", new Date());

        return "qlstk/client/mo-so-tiet-kiem";
    }

    // Xử lý submit form mở sổ
    @PostMapping("/mo-so-tiet-kiem")
    public String xuLyMoSo(@RequestParam String loaiSoId,
                           @RequestParam double soTienGui,
                           HttpServletRequest request,
                           Model model) {
        // Lấy userId từ JWT cookie (giả sử có util hỗ trợ)
        String khachHangId = JwtUtil.getUserIdFromCookie(request);

        LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(loaiSoId);

        YeuCauMoSo yc = new YeuCauMoSo();
        yc.setKhachHangId(khachHangId);
        yc.setLoaiSoId(loaiSoId);
        yc.setSoTienGuiBanDau(soTienGui);
        yc.setTrangThai("CHO");
        yc.setCreatedAt(new Date());
        yc.setLaiSuatApDung(laiSuat != null ? laiSuat.getLaiSuatNam() : null);

        yeuCauMoSoRepository.save(yc);

        model.addAttribute("message", "Yêu cầu mở sổ đã được gửi thành công!");

        return "redirect:/tra-cuu-yeu-cau";
    }
}
