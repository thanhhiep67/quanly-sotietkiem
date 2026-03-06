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

@Controller
@RequestMapping("/admin/quan-ly-loai-tiet-kiem")
public class AdminQLLsTkController {

    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;

    @Autowired
    private LaiSuatRepository laiSuatRepository;

    // Hiển thị danh sách loại sổ tiết kiệm
    @GetMapping
    public String listLoaiSo(Model model) {
        List<LoaiSoTK> list = loaiSoTKRepository.findAll();

        // map loại sổ với lãi suất mới nhất
        Map<String, Double> laiSuatMap = new HashMap<>();
        for (LoaiSoTK loai : list) {
            List<LaiSuat> laiSuats = laiSuatRepository.findByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            if (!laiSuats.isEmpty()) {
                laiSuatMap.put(loai.getId(), laiSuats.get(0).getLaiSuatNam());
            }
        }

        model.addAttribute("loaiSoList", list);
        model.addAttribute("laiSuatMap", laiSuatMap);
        return "qlstk/admin-dashboard/quan-ly-loai-tiet-kiem";
    }


    // Form thêm loại sổ
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("loaiSoTK", new LoaiSoTK());
        return "qlstk/admin-dashboard/them-loai-tiet-kiem";
    }

    // Xử lý thêm loại sổ
    @PostMapping("/them")
    public String addLoaiSo(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Lấy dữ liệu từ form
        String tenLoai = request.getParameter("tenLoai");
        Integer kyHanThang = Integer.valueOf(request.getParameter("kyHanThang"));
        boolean choGuiThem = request.getParameter("choGuiThem") != null;
        boolean choRutMotPhan = request.getParameter("choRutMotPhan") != null;
        Double laiSuatNam = Double.valueOf(request.getParameter("laiSuatNam"));

        // Tạo loại sổ
        LoaiSoTK loaiSo = new LoaiSoTK();
        loaiSo.setId(generateLoaiSoId());
        loaiSo.setTenLoai(tenLoai);
        loaiSo.setKyHanThang(kyHanThang);
        loaiSo.setChoGuiThem(choGuiThem);
        loaiSo.setChoRutMotPhan(choRutMotPhan);
        loaiSo.setCreatedAt(new Date());
        loaiSoTKRepository.save(loaiSo);

        // Tạo lãi suất ban đầu
        LaiSuat laiSuat = new LaiSuat();
        laiSuat.setLoaiStkId(loaiSo.getId());
        laiSuat.setLaiSuatNam(laiSuatNam);
        laiSuat.setNgayApDung(new Date());
        laiSuat.setCreatedAt(new Date());
        laiSuatRepository.save(laiSuat);

        redirectAttributes.addFlashAttribute("message", "Thêm loại tiết kiệm và lãi suất thành công!");
        return "redirect:/admin/quan-ly-loai-tiet-kiem";
    }



    // Xóa loại sổ
    @GetMapping("/xoa/{id}")
    public String deleteLoaiSo(@PathVariable String id,
                               RedirectAttributes redirectAttributes) {
        loaiSoTKRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Xóa loại tiết kiệm thành công!");
        return "redirect:/admin/quan-ly-loai-tiet-kiem";
    }


    public String generateLoaiSoId() {
        // Lấy số lượng hiện có
        long count = loaiSoTKRepository.count();
        // Sinh ID theo dạng LS + số thứ tự
        return String.format("LS%03d", count + 1);
    }
}
