package vn.edu.taydo.quanly_sotietkiem.DTO;

import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;

import java.util.Date;

public class YeuCauView {
    private String id;
    private double soTienGuiBanDau;
    private String trangThai;
    private Date createdAt;
    private Double laiSuatApDung;
    private Integer thoiHan;

    public YeuCauView(YeuCauMoSo yc, LoaiSoTK loai) {
        this.id = yc.getId();
        this.soTienGuiBanDau = yc.getSoTienGuiBanDau();
        this.trangThai = yc.getTrangThai();
        this.createdAt = yc.getCreatedAt();
        this.laiSuatApDung = yc.getLaiSuatApDung();
        this.thoiHan = loai.getKyHanThang();
    }

    // Getter chuẩn cho Thymeleaf
    public String getId() { return id; }
    public double getSoTienGuiBanDau() { return soTienGuiBanDau; }
    public String getTrangThai() { return trangThai; }
    public Date getCreatedAt() { return createdAt; }
    public Double getLaiSuatApDung() { return laiSuatApDung; }
    public Integer getThoiHan() { return thoiHan; }
}


