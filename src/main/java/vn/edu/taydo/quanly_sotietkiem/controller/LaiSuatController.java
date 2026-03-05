package vn.edu.taydo.quanly_sotietkiem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuatView;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LaiSuatController {

    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;

    @Autowired
    private LaiSuatRepository laiSuatRepository;

    @GetMapping("/lai-suat")
    public String laiSuat(Model model) {
        List<LoaiSoTK> loaiSoList = loaiSoTKRepository.findAll();

        // Lấy lãi suất mới nhất cho từng loại sổ
        List<LaiSuatView> viewList = new ArrayList<>();
        for (LoaiSoTK loai : loaiSoList) {
            LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
            viewList.add(new LaiSuatView(loai, laiSuat));
        }

        model.addAttribute("laiSuatList", viewList);
        model.addAttribute("ngayCapNhat", LocalDate.now());

        return "qlstk/client/lai-suat";
    }
}
