package vn.edu.taydo.quanly_sotietkiem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.taydo.quanly_sotietkiem.DTO.SoTietKiemView;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LaiSuatRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.time.LocalDate;
import java.util.*;

@Controller
public class GiaoDichController {

    @Autowired
    private SoTietKiemRepository soTietKiemRepository;

    @Autowired
    private LoaiSoTKRepository loaiSoTKRepository;

    @Autowired
    private GiaoDichRepository giaoDichRepository;

    @Autowired
    private LaiSuatRepository laiSuatRepository;

    // GET: hiển thị form rút tiền + lịch sử
    @GetMapping("/giao-dich")
    public String giaoDich(HttpServletRequest request, Model model) {
        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) {
            return "redirect:/login";
        }

        // danh sách sổ tiết kiệm
        List<SoTietKiem> list = soTietKiemRepository.findByKhachHangId(khachHangId);
        List<SoTietKiemView> viewList = new ArrayList<>();
        for (SoTietKiem so : list) {
            LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
            if (loai != null) {
                viewList.add(new SoTietKiemView(so, loai));
            }
        }
        model.addAttribute("soList", viewList);

        // lịch sử giao dịch gần đây
        List<GiaoDich> lichSu = giaoDichRepository.findByKhachHangIdOrderByCreatedAtDesc(khachHangId);
        model.addAttribute("lichSuGiaoDich", lichSu);


        return "qlstk/client/giao-dich";
    }

    // POST: xử lý rút tiền
    @PostMapping("/rut-tien")
    public String rutTien(HttpServletRequest request,
                          @RequestParam String soTkId,
                          @RequestParam double soTien,
                          RedirectAttributes ra) {
        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) {
            return "redirect:/login";
        }

        SoTietKiem so = soTietKiemRepository.findByMaSo(soTkId);
        if (so == null || !so.getKhachHangId().equals(khachHangId)) {
            ra.addFlashAttribute("error", "Không tìm thấy sổ tiết kiệm hợp lệ");
            return "redirect:/giao-dich";
        }

        LoaiSoTK loai = loaiSoTKRepository.findById(so.getLoaiSoId()).orElse(null);
        if (loai == null) {
            ra.addFlashAttribute("error", "Không xác định được loại sổ");
            return "redirect:/giao-dich";
        }

        Date now = new Date();
        double tienNhan = 0;
        double tienLai = 0;

        // Không kỳ hạn
        if (loai.getKyHanThang() == null || loai.getKyHanThang() == 0) {
            long days = (now.getTime() - so.getNgayMoSo().getTime()) / (1000 * 60 * 60 * 24);
            if (days < 15) {
                ra.addFlashAttribute("error", "Sổ không kỳ hạn chỉ được rút sau 15 ngày");
                return "redirect:/giao-dich";
            }
            if (soTien > so.getSoDuHienTai()) {
                ra.addFlashAttribute("error", "Số tiền rút vượt quá số dư");
                return "redirect:/giao-dich";
            }

            double laiSuatKhongKyHan = 0.005; // 0.5%/năm
            tienLai = soTien * (laiSuatKhongKyHan / 100.0 ) * (days / 365.0); // tính theo số ngày gửi
            tienNhan = soTien + tienLai; // cộng cả gốc + lãi

            so.setSoDuHienTai(so.getSoDuHienTai() - soTien);
            if (so.getSoDuHienTai() == 0) {
                so.setTrangThai("DONG");
            }
        }
        // Có kỳ hạn
        else {
            Date ngayDaoHan = so.getNgayDaoHan();
            if (ngayDaoHan == null) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn chưa có ngày đáo hạn");
                return "redirect:/giao-dich";
            }

            if (now.before(ngayDaoHan)) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn chỉ được rút khi đến hoặc quá ngày đáo hạn");
                return "redirect:/giao-dich";
            }
            if (soTien < so.getSoDuHienTai()) {
                ra.addFlashAttribute("error", "Sổ có kỳ hạn phải rút toàn bộ số dư");
                return "redirect:/giao-dich";
            }

            // Nếu đúng hạn → lãi suất kỳ hạn
            if (Math.abs(now.getTime() - ngayDaoHan.getTime()) < 24*60*60*1000) {
                LaiSuat laiSuat = laiSuatRepository.findTopByLoaiStkIdOrderByNgayApDungDesc(loai.getId());
                if (laiSuat == null) {
                    ra.addFlashAttribute("error", "Không tìm thấy lãi suất áp dụng cho loại sổ này");
                    return "redirect:/giao-dich";
                }
                double laiSuatKyHan = laiSuat.getLaiSuatNam();
                double months = loai.getKyHanThang();
                tienLai = so.getSoTienBanDau() * (laiSuatKyHan / 100.0) * (months / 12.0);
            } else {
                // quá hạn → lãi suất không kỳ hạn
                tienLai = so.getSoDuHienTai() * 0.005;
            }

            tienNhan = so.getSoDuHienTai() + tienLai;
            so.setSoDuHienTai(0);
            so.setTrangThai("DONG");
            so.setNgayDongSo(LocalDate.now());

        }

        soTietKiemRepository.save(so);

        // tạo giao dịch
        GiaoDich gd = new GiaoDich();
        gd.setSoTkId(soTkId);
        gd.setLoaiGiaoDich("RUT");
        gd.setNgayGiaoDich(now);
        gd.setSoTien(soTien);
        gd.setTienLaiPhatSinh(tienLai);
        gd.setTongTienNhan(tienNhan); // giờ đã cộng cả gốc + lãi
        gd.setCreatedAt(now);
        gd.setTrangThai("COMPLETED");
        gd.setGhiChu("Rút tiền tiết kiệm");
        gd.setKhachHangId(khachHangId);
        giaoDichRepository.save(gd);

        ra.addFlashAttribute("success", "Rút tiền thành công, nhận " + tienNhan + " đ");
        return "redirect:/giao-dich";
    }

    @PostMapping("/nap-tien")
    public String napTien(HttpServletRequest request,
                          @RequestParam String soTkId,
                          @RequestParam double soTien,
                          RedirectAttributes ra) {
        String khachHangId = JwtUtil.getUserIdFromCookie(request);
        if (khachHangId == null) {
            return "redirect:/login";
        }

        SoTietKiem so = soTietKiemRepository.findByMaSo(soTkId);
        if (so == null || !so.getKhachHangId().equals(khachHangId)) {
            ra.addFlashAttribute("errorNap", "Không tìm thấy sổ tiết kiệm hợp lệ");
            return "redirect:/giao-dich";
        }

        if (!"MO".equals(so.getTrangThai())) {
            ra.addFlashAttribute("errorNap", "Sổ không còn hoạt động để nạp thêm tiền");
            return "redirect:/giao-dich";
        }

        // Cập nhật số dư
        so.setSoDuHienTai(so.getSoDuHienTai() + soTien);
        soTietKiemRepository.save(so);

        // Lưu giao dịch nạp tiền
        GiaoDich gd = new GiaoDich();
        gd.setKhachHangId(khachHangId);
        gd.setSoTkId(soTkId);
        gd.setSoTien(soTien);
        gd.setLoaiGiaoDich("NAP");
        gd.setTrangThai("COMPLETED");
        gd.setCreatedAt(new Date());
        gd.setNgayGiaoDich(new Date());
        giaoDichRepository.save(gd);

        ra.addFlashAttribute("successNap", "Nạp tiền thành công!");
        return "redirect:/giao-dich";
    }


}
