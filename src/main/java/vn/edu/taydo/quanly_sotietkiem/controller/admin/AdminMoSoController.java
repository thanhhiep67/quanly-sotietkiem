package vn.edu.taydo.quanly_sotietkiem.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.model.*;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class AdminMoSoController {


    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    LoaiSoTKRepository loaiSoTKRepository;
    @Autowired
    LaiSuatRepository laiSuatRepository;
    @Autowired
    SoTietKiemRepository soTietKiemRepository;

    @GetMapping("/admin/mo-so")
    public String getMoSo(Model model) {
        List<KhachHang> khachHangList = khachHangRepository.findAll();
        List<LoaiSoTK> loaiSoList = loaiSoTKRepository.findAll();

        model.addAttribute("khachHangList", khachHangList);
        model.addAttribute("loaiSoList", loaiSoList);

        // Quan trọng: thêm object rỗng để Thymeleaf binding
        model.addAttribute("soTietKiem", new SoTietKiem());

        return "qlstk/admin-dashboard/mo-so-tiet-kiem";
    }

    @PostMapping("/admin/mo-so")
    public String submitMoSo(@RequestParam String cccd,
                             @ModelAttribute SoTietKiem soTietKiem,
                             RedirectAttributes redirectAttributes) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByCccd(cccd);
        if (khachHangOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng với CCCD này!");
            return "redirect:/admin/mo-so";
        }

        KhachHang khachHang = khachHangOpt.get();

        // Tạo mới sổ tiết kiệm, không dùng object bind sẵn
        SoTietKiem stk = new SoTietKiem();
        //stk.setId(null); // đảm bảo insert
        stk.setKhachHangId(khachHang.getId());
        stk.setLoaiSoId(soTietKiem.getLoaiSoId());
        stk.setSoTienBanDau(soTietKiem.getSoTienBanDau());

        // Lãi suất
        LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(stk.getLoaiSoId());
        if (laiSuat != null) {
            stk.setLaiSuatApDung(laiSuat.getLaiSuatNam());
        }

        // Mã sổ
        String maSo = "STK-" + khachHang.getId() + "-" + System.currentTimeMillis();

        stk.setMaSo(maSo);


        // Ngày mở, trạng thái, số dư
        Date ngayMoSo = new Date();
        stk.setTrangThai("MO");
        stk.setNgayMoSo(ngayMoSo);
        stk.setSoDuHienTai(stk.getSoTienBanDau());

        // Ngày đáo hạn
        LoaiSoTK loai = loaiSoTKRepository.findById(stk.getLoaiSoId()).orElse(null);
        if (loai != null && loai.getKyHanThang() != null && loai.getKyHanThang() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(ngayMoSo);
            cal.add(Calendar.MONTH, loai.getKyHanThang());
            stk.setNgayDaoHan(cal.getTime());
        }

        // Lưu mới
        soTietKiemRepository.save(stk);

        redirectAttributes.addFlashAttribute("message",
                "Khách hàng " + khachHang.getHoten() + " đã mở sổ tiết kiệm thành công!");
        return "redirect:/admin/mo-so";
    }


}
