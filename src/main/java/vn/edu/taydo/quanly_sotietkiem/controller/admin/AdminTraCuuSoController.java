package vn.edu.taydo.quanly_sotietkiem.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.taydo.quanly_sotietkiem.DTO.SoTietKiemDTO;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/tra-cuu-so")
public class AdminTraCuuSoController {

    @Autowired
    private SoTietKiemRepository soTietKiemRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    // Trang tra cứu ban đầu
    @GetMapping
    public String getTraCuuPage(Model model) {
        List<SoTietKiem> soList = soTietKiemRepository.findAll();

        List<SoTietKiemDTO> dtoList = soList.stream().map(so -> {
            String tenKhachHang = khachHangRepository.findById(so.getKhachHangId())
                    .map(KhachHang::getHoten)
                    .orElse("Không rõ");
            return new SoTietKiemDTO(
                    so.getMaSo(),
                    tenKhachHang,
                    so.getLoaiSoId(),
                    so.getSoDuHienTai(),
                    so.getTrangThai()
            );
        }).toList();

        model.addAttribute("soList", dtoList);
        return "qlstk/admin-dashboard/tra-cuu-so";
    }


    @GetMapping("/tim-kiem")
    public String timKiem(@RequestParam(required = false) String cccd, Model model) {
        List<SoTietKiemDTO> dtoList = new ArrayList<>();

        if (cccd != null && !cccd.trim().isEmpty()) {
            // tìm khách hàng theo CCCD
            KhachHang khachHang = khachHangRepository.findByCccd(cccd).orElse(null);

            if (khachHang != null) {
                // tìm tất cả sổ của khách hàng này
                List<SoTietKiem> soList = soTietKiemRepository.findByKhachHangId(khachHang.getId());

                dtoList = soList.stream().map(so -> new SoTietKiemDTO(
                        so.getMaSo(),
                        khachHang.getHoten(),
                        so.getLoaiSoId(),
                        so.getSoDuHienTai(),
                        so.getTrangThai()
                )).toList();
            }
        }

        if (dtoList.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy sổ nào cho CCCD: " + cccd);
        }

        model.addAttribute("soList", dtoList);
        model.addAttribute("keyword", cccd);
        return "qlstk/admin-dashboard/tra-cuu-so";
    }



}

