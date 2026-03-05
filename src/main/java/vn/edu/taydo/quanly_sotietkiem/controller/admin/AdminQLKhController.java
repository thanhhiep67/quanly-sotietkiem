package vn.edu.taydo.quanly_sotietkiem.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.TaiKhoan;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.TaiKhoanRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/quan-ly-khach-hang")
public class AdminQLKhController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    // Hiển thị danh sách khách hàng
    @GetMapping
    public String quanLyKhachHang(Model model) {
        List<KhachHang> list = khachHangRepository.findAll();
        model.addAttribute("khachHangList", list);
        return "qlstk/admin-dashboard/quan-ly-khach-hang";
    }

    // Form thêm khách hàng
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("khachHang", new KhachHang());
        return "qlstk/admin-dashboard/them-khach-hang";
    }

    // Xử lý thêm khách hàng
    @PostMapping("/them")
    public String addKhachHang(@ModelAttribute KhachHang khachHang,
                               RedirectAttributes redirectAttributes) {
        long count = khachHangRepository.count();
        khachHang.setTaikhoan_id("KH-" + String.format("%03d", count + 1));
        khachHang.setCreatedAt(LocalDateTime.now());

        khachHangRepository.save(khachHang);
        redirectAttributes.addFlashAttribute("message", "Thêm khách hàng thành công!");
        return "redirect:/admin/quan-ly-khach-hang";
    }

    // Xóa khách hàng
    @GetMapping("/xoa/{id}")
    public String deleteKhachHang(@PathVariable String id,
                                  RedirectAttributes redirectAttributes) {
        khachHangRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Xóa khách hàng thành công!");
        return "redirect:/admin/quan-ly-khach-hang";
    }

    @GetMapping("/tim-kiem")
    public String timKiemTheoCccd(@RequestParam("cccd") String cccd, Model model) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByCccd(cccd);

        if (khachHangOpt.isPresent()) {
            model.addAttribute("khachHangList", List.of(khachHangOpt.get()));
        } else {
            model.addAttribute("khachHangList", List.of());
            model.addAttribute("error", "Không tìm thấy khách hàng với CCCD: " + cccd);
        }

        return "qlstk/admin-dashboard/quan-ly-khach-hang";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<KhachHang> khOpt = khachHangRepository.findById(id);
        if (khOpt.isPresent()) {
            model.addAttribute("khachHang", khOpt.get());
            return "qlstk/admin-dashboard/edit-khach-hang";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
            return "redirect:/admin/quan-ly-khach-hang";
        }
    }

    @PostMapping("/edit")
    public String updateKhachHang(@ModelAttribute KhachHang khachHang, RedirectAttributes redirectAttributes) {
        // kiểm tra trùng CCCD
        Optional<KhachHang> duplicate = khachHangRepository.findByCccd(khachHang.getCccd());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(khachHang.getId())) {
            redirectAttributes.addFlashAttribute("error", "CCCD đã tồn tại!");
            return "redirect:/admin/quan-ly-khach-hang/edit/" + khachHang.getId();
        }

        // lấy khách hàng gốc theo ID
        Optional<KhachHang> existingOpt = khachHangRepository.findById(khachHang.getId());
        if (existingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
            return "redirect:/admin/quan-ly-khach-hang";
        }

        KhachHang existing = existingOpt.get();

        // chỉ update nếu khác null và khác giá trị cũ
        if (khachHang.getHoten() != null && !khachHang.getHoten().equals(existing.getHoten())) {
            existing.setHoten(khachHang.getHoten());
        }
        if (khachHang.getGioiTinh() != null && !khachHang.getGioiTinh().equals(existing.getGioiTinh())) {
            existing.setGioiTinh(khachHang.getGioiTinh());
        }
        if (khachHang.getSdt() != null && !khachHang.getSdt().equals(existing.getSdt())) {
            existing.setSdt(khachHang.getSdt());
        }
        if (khachHang.getCccd() != null && !khachHang.getCccd().equals(existing.getCccd())) {
            existing.setCccd(khachHang.getCccd());
        }
        if (khachHang.getDiaChi() != null && !khachHang.getDiaChi().equals(existing.getDiaChi())) {
            existing.setDiaChi(khachHang.getDiaChi());
        }
        if (khachHang.getEmail() != null && !khachHang.getEmail().equals(existing.getEmail())) {
            existing.setEmail(khachHang.getEmail());
        }
        if (khachHang.getNgaySinh() != null && !khachHang.getNgaySinh().equals(existing.getNgaySinh())) {
            existing.setNgaySinh(khachHang.getNgaySinh());
        }

        // giữ nguyên createdAt, taikhoanId
        khachHangRepository.save(existing);

        redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
        return "redirect:/admin/quan-ly-khach-hang";
    }




}

