package vn.edu.taydo.quanly_sotietkiem.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SoTietKiemDTO {
    private String maSo;
    private String tenKhachHang;
    private String loaiSoId;
    private String tenLoaiSo;
    private double soDuHienTai;
    private String trangThai;

    // constructor
    public SoTietKiemDTO(String maSo, String tenKhachHang, String loaiSoId, double soDuHienTai, String trangThai) {
        this.maSo = maSo;
        this.tenKhachHang = tenKhachHang;
        this.loaiSoId = loaiSoId;
        this.soDuHienTai = soDuHienTai;
        this.trangThai = trangThai;
    }

    // getter/setter
}

