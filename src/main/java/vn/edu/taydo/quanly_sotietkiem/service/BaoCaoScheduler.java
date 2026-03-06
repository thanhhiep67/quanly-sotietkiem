package vn.edu.taydo.quanly_sotietkiem.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BaoCaoScheduler {

    private final BaoCaoService baoCaoService;

    public BaoCaoScheduler(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    // Chạy mỗi ngày lúc 23:59
    @Scheduled(cron = "0 59 23 * * *")
    public void chotBaoCaoCuoiNgay() {
        LocalDate ngay = LocalDate.now();
        baoCaoService.taoBaoCaoNgay(ngay);
        System.out.println("Đã tạo báo cáo cho ngày: " + ngay);
    }
}