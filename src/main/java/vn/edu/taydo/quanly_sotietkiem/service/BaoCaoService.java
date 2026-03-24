package vn.edu.taydo.quanly_sotietkiem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.model.BaoCaoNgay;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;
import vn.edu.taydo.quanly_sotietkiem.repository.BaoCaoNgayRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class BaoCaoService {

    @Autowired private GiaoDichRepository    giaoDichRepository;
    @Autowired private SoTietKiemRepository  soTietKiemRepository;
    @Autowired private BaoCaoNgayRepository  baoCaoNgayRepository;

    // ─────────────────────────────────────────────────────────
    //  Helper: chuyển LocalDate → Date với giờ cụ thể
    //  Dùng START_OF_DAY (00:00:00.000) và END_OF_DAY (23:59:59.999)
    //  thay vì plusDays(1) để tránh lấy lẫn giao dịch ngày hôm sau
    // ─────────────────────────────────────────────────────────
    private Date toDate(LocalDate date, LocalTime time) {
        return Date.from(date.atTime(time)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    // ─────────────────────────────────────────────────────────
    //  Tạo báo cáo cho một ngày
    // ─────────────────────────────────────────────────────────
    public BaoCaoNgay taoBaoCaoNgay(LocalDate ngay) {

        Date startOfDay = toDate(ngay, LocalTime.MIN);           // 00:00:00.000
        Date endOfDay   = toDate(ngay, LocalTime.MAX);           // 23:59:59.999999999

        // ── Tổng thu (NAP) ───────────────────────────────────
        List<GiaoDich> thuList = giaoDichRepository
                .findByLoaiGiaoDichAndNgayGiaoDichBetween("NAP", startOfDay, endOfDay);
        double tongThu = thuList.stream()
                .mapToDouble(GiaoDich::getSoTien)
                .sum();

        // ── Tổng chi (RUT) ───────────────────────────────────
        List<GiaoDich> chiList = giaoDichRepository
                .findByLoaiGiaoDichAndNgayGiaoDichBetween("RUT", startOfDay, endOfDay);
        double tongChi = chiList.stream()
                .mapToDouble(GiaoDich::getTongTienNhan)
                .sum();

        double chenhLech = tongThu - tongChi;

        // ── Sổ mở / đóng ─────────────────────────────────────
        long soMo   = soTietKiemRepository.countByNgayMoSoBetween(startOfDay, endOfDay);
        long soDong = soTietKiemRepository.countByNgayDongSoBetween(startOfDay, endOfDay);

        // ── Lưu báo cáo ──────────────────────────────────────
        BaoCaoNgay baoCao = baoCaoNgayRepository.findByNgay(ngay)
                .orElse(new BaoCaoNgay());

        baoCao.setNgay(ngay);
        baoCao.setTongThu(tongThu);
        baoCao.setTongChi(tongChi);
        baoCao.setChenhLech(chenhLech);
        baoCao.setSoMo(soMo);
        baoCao.setSoDong(soDong);
        baoCao.setChenhLechSo(soMo - soDong);

        return baoCaoNgayRepository.save(baoCao);
    }

    // ─────────────────────────────────────────────────────────
    //  Lấy báo cáo trong khoảng ngày
    // ─────────────────────────────────────────────────────────
    public List<BaoCaoNgay> layBaoCao(LocalDate start, LocalDate end) {
        return baoCaoNgayRepository.findByNgayBetween(start, end);
    }

    // ─────────────────────────────────────────────────────────
    //  Lấy ngày cuối cùng đã có báo cáo
    // ─────────────────────────────────────────────────────────
    public LocalDate layNgayCuoiBaoCao() {
        BaoCaoNgay baoCao = baoCaoNgayRepository.findTopByOrderByNgayDesc();
        return baoCao != null ? baoCao.getNgay() : LocalDate.now().minusDays(1);
    }

    // ─────────────────────────────────────────────────────────
    //  Tạo báo cáo bù cho các ngày bị thiếu
    //
    //  Lưu ý: vòng lặp dừng TRƯỚC hôm nay (isBefore)
    //  vì báo cáo hôm nay chưa hoàn chỉnh (ngày chưa kết thúc)
    //  Nếu muốn tạo luôn cho hôm nay thì đổi thành isBeforeOrEqual
    // ─────────────────────────────────────────────────────────
    public void taoBaoCaoBu() {
        LocalDate homNay   = LocalDate.now();
        LocalDate ngayCuoi = layNgayCuoiBaoCao();

        // Tạo báo cáo cho từng ngày còn thiếu, không bao gồm hôm nay
        LocalDate ngayCanTao = ngayCuoi.plusDays(1);
        while (ngayCanTao.isBefore(homNay)) {
            taoBaoCaoNgay(ngayCanTao);
            System.out.println("Đã tạo báo cáo bù cho ngày: " + ngayCanTao);
            ngayCanTao = ngayCanTao.plusDays(1);
        }

        // Luôn tạo/cập nhật báo cáo hôm qua (ngày gần nhất hoàn chỉnh)
        LocalDate homQua = homNay.minusDays(1);
        if (!homQua.isBefore(ngayCuoi)) {
            taoBaoCaoNgay(homQua);
            System.out.println("Đã cập nhật báo cáo hôm qua: " + homQua);
        }
    }
}