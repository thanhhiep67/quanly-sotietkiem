package vn.edu.taydo.quanly_sotietkiem.DTO;

import lombok.Getter;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;

import java.util.Date;

@Getter
public class YeuCauViewAdmin {
    private String id;
    private String khachHangId;
    private String tenKhachHang;
    private double soTienGuiBanDau;
    private String trangThai;
    private Date createdAt;
    private Double laiSuatApDung;
    private Integer thoiHan;

    public YeuCauViewAdmin(YeuCauMoSo yc, LoaiSoTK loai, KhachHang kh) {
        this.id = yc.getId();
        this.khachHangId = yc.getKhachHangId();
        this.soTienGuiBanDau = yc.getSoTienGuiBanDau();
        this.tenKhachHang = kh.getHoten();
        this.trangThai = yc.getTrangThai();
        this.createdAt = yc.getCreatedAt();
        this.laiSuatApDung = yc.getLaiSuatApDung();
        this.thoiHan = loai.getKyHanThang();
    }
}
