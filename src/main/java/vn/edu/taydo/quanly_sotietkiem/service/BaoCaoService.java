package vn.edu.taydo.quanly_sotietkiem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.model.BaoCaoNgay;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;
import vn.edu.taydo.quanly_sotietkiem.repository.BaoCaoNgayRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.GiaoDichRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class BaoCaoService {

    @Autowired
    private GiaoDichRepository giaoDichRepository;

    @Autowired
    private SoTietKiemRepository soTietKiemRepository;

    @Autowired
    private BaoCaoNgayRepository baoCaoNgayRepository;

    // Tạo báo cáo cho một ngày
    public BaoCaoNgay taoBaoCaoNgay(LocalDate ngay) {
        ZoneId zoneId = ZoneId.systemDefault();

        Date startOfDay = Date.from(ngay.atStartOfDay(zoneId).toInstant());
        Date endOfDay = Date.from(ngay.plusDays(1).atStartOfDay(zoneId).toInstant());

        // ===== TỔNG THU =====
        List<GiaoDich> thuList = giaoDichRepository
                .findByLoaiGiaoDichAndNgayGiaoDichBetween("NAP", startOfDay, endOfDay);

        double tongThu = thuList.stream()
                .mapToDouble(GiaoDich::getSoTien)
                .sum();

        // ===== TỔNG CHI =====
        List<GiaoDich> chiList = giaoDichRepository
                .findByLoaiGiaoDichAndNgayGiaoDichBetween("RUT", startOfDay, endOfDay);

        double tongChi = chiList.stream()
                .mapToDouble(GiaoDich::getTongTienNhan)
                .sum();

        double chenhLech = tongThu - tongChi;

        // ===== SỔ MỞ - ĐÓNG =====
        long soMo = soTietKiemRepository.countByNgayMoSoBetween(startOfDay, endOfDay);
        long soDong = soTietKiemRepository.countByNgayDongSoBetween(startOfDay, endOfDay);
        long chenhLechSo = soMo - soDong;

        // ===== LƯU BÁO CÁO =====
        BaoCaoNgay baoCao = baoCaoNgayRepository.findByNgay(ngay)
                .orElse(new BaoCaoNgay());

        baoCao.setNgay(ngay);
        baoCao.setTongThu(tongThu);
        baoCao.setTongChi(tongChi);
        baoCao.setChenhLech(chenhLech);
        baoCao.setSoMo(soMo);
        baoCao.setSoDong(soDong);
        baoCao.setChenhLechSo(chenhLechSo);

        return baoCaoNgayRepository.save(baoCao);
    }

    // Lấy báo cáo trong khoảng ngày
    public List<BaoCaoNgay> layBaoCao(LocalDate start, LocalDate end) {
        return baoCaoNgayRepository.findByNgayBetween(start, end);
    }

    // Lấy ngày cuối cùng đã có báo cáo
    public LocalDate layNgayCuoiBaoCao() {
        BaoCaoNgay baoCao = baoCaoNgayRepository.findTopByOrderByNgayDesc();
        if (baoCao != null) {
            return baoCao.getNgay();
        }
        return LocalDate.now().minusDays(1);
    }


    // Tạo báo cáo bù cho các ngày bị thiếu
    public void taoBaoCaoBu() {
        LocalDate homNay = LocalDate.now();
        LocalDate ngayCuoi = layNgayCuoiBaoCao();

        while (ngayCuoi.isBefore(homNay)) {
            ngayCuoi = ngayCuoi.plusDays(1);
            taoBaoCaoNgay(ngayCuoi);
            System.out.println("Đã tạo báo cáo bù cho ngày: " + ngayCuoi);
        }
    }
}
