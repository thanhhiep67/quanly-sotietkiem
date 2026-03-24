package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/quan-ly-loai-tiet-kiem")
public class AdminQLLsTkController {

    @Autowired private LoaiSoTKRepository loaiSoTKRepository;
    @Autowired private LaiSuatRepository  laiSuatRepository;

    // ─────────────────────────────────────────────────────────
    //  GET /  →  danh sách
    // ─────────────────────────────────────────────────────────
    @GetMapping
    public String listLoaiSo(Model model) {
        List<LoaiSoTK> list = loaiSoTKRepository.findAll();

        Map<String, Double> laiSuatMap = new HashMap<>();
        for (LoaiSoTK loai : list) {
            List<LaiSuat> laiSuats = laiSuatRepository
                    .findByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            if (!laiSuats.isEmpty()) {
                laiSuatMap.put(loai.getId(), laiSuats.get(0).getLaiSuatNam());
            }
        }

        model.addAttribute("loaiSoList",  list);
        model.addAttribute("laiSuatMap",  laiSuatMap);
        return "qlstk/admin-dashboard/quan-ly-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /them  →  form thêm mới
    // ─────────────────────────────────────────────────────────
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("loaiSoTK", new LoaiSoTK());
        return "qlstk/admin-dashboard/them-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /them  →  lưu loại mới
    // ─────────────────────────────────────────────────────────
    @PostMapping("/them")
    public String addLoaiSo(HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        String  tenLoai      = request.getParameter("tenLoai");
        Integer kyHanThang   = Integer.valueOf(request.getParameter("kyHanThang"));
        boolean choGuiThem   = request.getParameter("choGuiThem")   != null;
        boolean choRutMotPhan= request.getParameter("choRutMotPhan")!= null;
        Double  laiSuatNam   = Double.valueOf(request.getParameter("laiSuatNam"));

        LoaiSoTK loaiSo = new LoaiSoTK();
        loaiSo.setId(generateLoaiSoId());
        loaiSo.setTenLoai(tenLoai);
        loaiSo.setKyHanThang(kyHanThang);
        loaiSo.setChoGuiThem(choGuiThem);
        loaiSo.setChoRutMotPhan(choRutMotPhan);
        loaiSo.setCreatedAt(new Date());
        loaiSoTKRepository.save(loaiSo);

        LaiSuat laiSuat = new LaiSuat();
        laiSuat.setLoaiStkId(loaiSo.getId());
        laiSuat.setLaiSuatNam(laiSuatNam);
        laiSuat.setNgayApDung(new Date());
        laiSuat.setCreatedAt(new Date());
        laiSuatRepository.save(laiSuat);

        redirectAttributes.addFlashAttribute("message", "Thêm loại tiết kiệm thành công!");
        return "redirect:/admin/quan-ly-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /sua/{id}  →  form chỉnh sửa
    // ─────────────────────────────────────────────────────────
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable String id, Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<LoaiSoTK> opt = loaiSoTKRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy loại tiết kiệm!");
            return "redirect:/admin/quan-ly-loai-tiet-kiem";
        }

        LoaiSoTK loai = opt.get();

        // Lãi suất hiện tại
        List<LaiSuat> laiSuats = laiSuatRepository
                .findByLoaiStkIdOrderByNgayApDungDesc(id);
        Double laiSuatHienTai = laiSuats.isEmpty() ? 0.0 : laiSuats.get(0).getLaiSuatNam();

        model.addAttribute("loaiSoTK",       loai);
        model.addAttribute("laiSuatHienTai", laiSuatHienTai);
        return "qlstk/admin-dashboard/sua-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  POST /sua/{id}  →  lưu thay đổi
    // ─────────────────────────────────────────────────────────
    @PostMapping("/sua/{id}")
    public String editLoaiSo(@PathVariable String id,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        Optional<LoaiSoTK> opt = loaiSoTKRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy loại tiết kiệm!");
            return "redirect:/admin/quan-ly-loai-tiet-kiem";
        }

        LoaiSoTK loai = opt.get();

        // ── Cập nhật thông tin cơ bản ────────────────────────
        String  tenLoai      = request.getParameter("tenLoai");
        Integer kyHanThang   = Integer.valueOf(request.getParameter("kyHanThang"));
        boolean choGuiThem   = request.getParameter("choGuiThem")   != null;
        boolean choRutMotPhan= request.getParameter("choRutMotPhan")!= null;
        Double  laiSuatMoi   = Double.valueOf(request.getParameter("laiSuatNam"));

        loai.setTenLoai(tenLoai);
        loai.setKyHanThang(kyHanThang);
        loai.setChoGuiThem(choGuiThem);
        loai.setChoRutMotPhan(choRutMotPhan);
        loaiSoTKRepository.save(loai);

        // ── Cập nhật lãi suất nếu thay đổi ──────────────────
        List<LaiSuat> laiSuats = laiSuatRepository
                .findByLoaiStkIdOrderByNgayApDungDesc(id);
        double laiSuatCu = laiSuats.isEmpty() ? -1 : laiSuats.get(0).getLaiSuatNam();

        if (laiSuatMoi != laiSuatCu) {
            // Tạo bản ghi lãi suất mới (giữ lịch sử)
            LaiSuat laiSuatMoiRecord = new LaiSuat();
            laiSuatMoiRecord.setLoaiStkId(id);
            laiSuatMoiRecord.setLaiSuatNam(laiSuatMoi);
            laiSuatMoiRecord.setNgayApDung(new Date());
            laiSuatMoiRecord.setCreatedAt(new Date());
            laiSuatRepository.save(laiSuatMoiRecord);
        }

        redirectAttributes.addFlashAttribute("message",
                "Cập nhật loại tiết kiệm \"" + tenLoai + "\" thành công!");
        return "redirect:/admin/quan-ly-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  GET /xoa/{id}  →  xóa
    // ─────────────────────────────────────────────────────────
    @GetMapping("/xoa/{id}")
    public String deleteLoaiSo(@PathVariable String id,
                               RedirectAttributes redirectAttributes) {
        loaiSoTKRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Xóa loại tiết kiệm thành công!");
        return "redirect:/admin/quan-ly-loai-tiet-kiem";
    }

    // ─────────────────────────────────────────────────────────
    //  Helper: sinh ID
    // ─────────────────────────────────────────────────────────
    public String generateLoaiSoId() {
        long count = loaiSoTKRepository.count();
        return String.format("LS%03d", count + 1);
    }
}