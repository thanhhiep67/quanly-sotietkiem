package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.model.BaoCaoNgay;
import vn.edu.taydo.quanly_sotietkiem.repository.BaoCaoNgayRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminBaoCaoThongKeController {

    private final BaoCaoNgayRepository baoCaoNgayRepository;

    public AdminBaoCaoThongKeController(BaoCaoNgayRepository baoCaoNgayRepository) {
        this.baoCaoNgayRepository = baoCaoNgayRepository;
    }

    @GetMapping("/admin/bao-cao-thong-ke")
    public String getBaoCaoThongKe(Model model) {
        List<BaoCaoNgay> last7Days = baoCaoNgayRepository.findAll().stream()
                .sorted(Comparator.comparing(BaoCaoNgay::getNgay).reversed())
                .limit(7)
                .collect(Collectors.toList());

        // Chuẩn bị dữ liệu cho Chart.js
        List<String> labels = last7Days.stream()
                .map(b -> b.getNgay().format(DateTimeFormatter.ofPattern("dd/MM")))
                .collect(Collectors.toList());

        List<Double> tongThu = last7Days.stream().map(BaoCaoNgay::getTongThu).toList();
        List<Double> tongChi = last7Days.stream().map(BaoCaoNgay::getTongChi).toList();
        List<Double> chenhLech = last7Days.stream().map(BaoCaoNgay::getChenhLech).toList();

        model.addAttribute("baoCaoList", last7Days);
        model.addAttribute("labels", labels);
        model.addAttribute("tongThu", tongThu);
        model.addAttribute("tongChi", tongChi);
        model.addAttribute("chenhLech", chenhLech);

        return "qlstk/admin-dashboard/bao-cao-thong-ke";
    }

}
